package com.goldenboot.goldenbootfinancial

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application { configureRouting() }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Server is running", response.bodyAsText())
    }

    @Test
    fun testHealth() = testApplication {
        application { configureRouting() }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }
}

class BffRoutesTest {
    private val fullProfile = """
        {"household":{},"cashFlow":{},"foundation":{},"accountPreferences":[],"goals":[],"risk":{}}
    """.trimIndent()

    private val json = ContentType.Application.Json

    /** An upstream stand-in: records the Authorization header and answers by path. */
    private class FakeUpstream {
        var seenAuth: String? = "<not called>"
        val client = HttpClient(MockEngine { req ->
            seenAuth = req.headers[HttpHeaders.Authorization]
            val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
            when (req.url.encodedPath) {
                "/profile/status" -> respond(
                    """{"saved":true,"version":3,"updated_at":"2026-10-09T10:00:00Z","storage":"x"}""",
                    HttpStatusCode.OK, jsonHeaders
                )
                "/profile" -> when (req.method) {
                    HttpMethod.Put -> respond(
                        """{"status":"saved","version":4,"updated_at":"2026-10-09T11:00:00Z"}""",
                        HttpStatusCode.OK, jsonHeaders
                    )
                    else -> respond(
                        """{"detail":"No server-side Golden Boot profile has been saved yet."}""",
                        HttpStatusCode.NotFound, jsonHeaders
                    )
                }
                "/legal/status" -> respond(
                    """{"documents":[],"outstanding":[],"all_documents_accepted":true,"intake_complete":true,
                       "questionnaire_complete":false,"beta_onboarding_complete":true}""",
                    HttpStatusCode.OK, jsonHeaders
                )
                else -> respond("{}", HttpStatusCode.NotFound, jsonHeaders)
            }
        }) { install(ContentNegotiation) { json() } }
    }

    // ---- mock mode ---------------------------------------------------------

    @Test
    fun mockModeServesScreens() = testApplication {
        application { configureSerialization(); configureRouting(BffConfig(mode = "mock")) }
        listOf("dashboard", "cashflow", "payroll", "paycheckplan", "investments", "profile-items").forEach {
            assertEquals(HttpStatusCode.OK, client.get("/$it").status, it)
        }
    }

    @Test
    fun mockProfileRoundTrip() = testApplication {
        application { configureSerialization(); configureRouting(BffConfig(mode = "mock")) }

        assertEquals(HttpStatusCode.NotFound, client.get("/profile").status)
        assertTrue(client.get("/profile/status").bodyAsText().contains("\"saved\": false"))

        val incomplete = client.put("/profile") { contentType(json); setBody("""{"household":{}}""") }
        assertEquals(HttpStatusCode.BadRequest, incomplete.status)
        assertTrue(incomplete.bodyAsText().contains("missing required sections"))

        val saved = client.put("/profile") { contentType(json); setBody(fullProfile) }
        assertEquals(HttpStatusCode.OK, saved.status)
        assertTrue(saved.bodyAsText().contains("\"version\": 1"))

        val loaded = client.get("/profile")
        assertEquals(HttpStatusCode.OK, loaded.status)
        assertTrue(loaded.bodyAsText().contains("accountPreferences"))
        assertTrue(client.get("/profile/status").bodyAsText().contains("\"saved\": true"))
        assertEquals(HttpStatusCode.OK, client.get("/legal/status").status)
    }

    // ---- proxy mode --------------------------------------------------------

    @Test
    fun proxyRequiresAuthorization() = testApplication {
        val upstream = FakeUpstream()
        application {
            configureSerialization()
            configureRouting(BffConfig(mode = "proxy"), UpstreamClient("http://upstream", upstream.client))
        }
        listOf("/profile/status", "/profile", "/legal/status").forEach {
            assertEquals(HttpStatusCode.Unauthorized, client.get(it).status, it)
        }
        assertEquals("<not called>", upstream.seenAuth)
    }

    @Test
    fun proxyForwardsTokenAndMapsFields() = testApplication {
        val upstream = FakeUpstream()
        application {
            configureSerialization()
            configureRouting(BffConfig(mode = "proxy"), UpstreamClient("http://upstream", upstream.client))
        }
        val auth = { b: HttpRequestBuilder -> b.header(HttpHeaders.Authorization, "Bearer abc") }

        val status = client.get("/profile/status") { auth(this) }
        assertEquals(HttpStatusCode.OK, status.status)
        assertEquals("Bearer abc", upstream.seenAuth)
        val statusBody = status.bodyAsText()
        assertTrue(statusBody.contains("\"updatedAt\": \"2026-10-09T10:00:00Z\""), statusBody)
        assertTrue(statusBody.contains("\"version\": 3"))

        val legal = client.get("/legal/status") { auth(this) }.bodyAsText()
        assertTrue(legal.contains("\"questionnaireComplete\": false"), legal)
        assertTrue(legal.contains("\"betaOnboardingComplete\": true"))

        val saved = client.put("/profile") { auth(this); contentType(json); setBody(fullProfile) }
        assertEquals(HttpStatusCode.OK, saved.status)
        assertTrue(saved.bodyAsText().contains("\"version\": 4"))
    }

    @Test
    fun proxyRelaysUpstreamStatusAndDetail() = testApplication {
        val upstream = FakeUpstream()
        application {
            configureSerialization()
            configureRouting(BffConfig(mode = "proxy"), UpstreamClient("http://upstream", upstream.client))
        }
        val response = client.get("/profile") { header(HttpHeaders.Authorization, "Bearer abc") }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("No server-side Golden Boot profile"))
    }

    @Test
    fun proxyAllowsAnonymousOnlyWhenEnabled() = testApplication {
        val upstream = FakeUpstream()
        application {
            configureSerialization()
            configureRouting(
                BffConfig(mode = "proxy", allowAnonymous = true),
                UpstreamClient("http://upstream", upstream.client)
            )
        }
        assertEquals(HttpStatusCode.OK, client.get("/profile/status").status)
        assertNull(upstream.seenAuth)
    }

    @Test
    fun proxyAnswers501ForScreensNotWiredYet() = testApplication {
        val upstream = FakeUpstream()
        application {
            configureSerialization()
            configureRouting(BffConfig(mode = "proxy"), UpstreamClient("http://upstream", upstream.client))
        }
        listOf("cashflow", "payroll", "paycheckplan", "investments").forEach {
            assertEquals(HttpStatusCode.NotImplemented, client.get("/$it").status, it)
        }
        assertEquals("<not called>", upstream.seenAuth)
    }
}
