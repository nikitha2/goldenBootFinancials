package com.goldenboot.goldenbootfinancial.core.foundation

import com.goldenboot.goldenbootfinancial.core.auth.AuthState
import com.goldenboot.goldenbootfinancial.core.auth.SessionManager
import com.goldenboot.goldenbootfinancial.core.auth.SignInProvider
import com.goldenboot.goldenbootfinancial.core.auth.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import com.goldenboot.goldenbootfinancial.core.network.ApiException
import com.goldenboot.goldenbootfinancial.core.network.BearerAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** A scriptable [SignInProvider]. */
class FakeSignInProvider(
    override val isAvailable: Boolean = true,
    var token: String? = null,
    var nextResult: SignInResult = SignInResult.Success
) : SignInProvider {
    override val hasSession = MutableStateFlow(false)
    var signOutCalls = 0
    override suspend fun signIn(): SignInResult {
        if (nextResult is SignInResult.Success) { hasSession.value = true; if (token == null) token = "tok" }
        return nextResult
    }
    override suspend fun accessToken(): String? = if (hasSession.value) token else null
    override suspend fun signOut() { signOutCalls++; hasSession.value = false; token = null }
}

class FoundationTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private class Harness(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
        val provider = FakeSignInProvider()
        val session = SessionManager(provider)
        val requests = mutableListOf<HttpRequestData>()
        val client = HttpClient(MockEngine { request ->
            requests += request
            handler(request)
        }) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            install(BearerAuth) {
                tokenProvider = session
                onUnauthorized = session::onUnauthorized
            }
        }
        val api = FoundationApi(client)
    }

    // ---- bearer token -----------------------------------------------------

    @Test
    fun attachesBearerTokenOnlyWhenSignedIn() = runTest {
        val h = Harness { respond("""{"saved":true,"version":1}""", HttpStatusCode.OK, jsonHeaders) }

        h.api.profileStatus()
        assertNull(h.requests.last().headers[HttpHeaders.Authorization])

        h.provider.token = "tok123"
        h.provider.hasSession.value = true
        h.api.profileStatus()
        assertEquals("Bearer tok123", h.requests.last().headers[HttpHeaders.Authorization])
        assertEquals(true, h.provider.hasSession.value)
    }

    @Test
    fun unauthorizedResponseSignsTheMemberOut() = runTest {
        val h = Harness { respond("""{"error":"Sign in required."}""", HttpStatusCode.Unauthorized, jsonHeaders) }
        h.provider.token = "expired"

        val error = assertFailsWith<ApiException> { h.api.profileStatus() }

        assertTrue(error.isUnauthorized)
        assertEquals("Sign in required.", error.message)
        assertEquals(AuthState.SignedOut, h.session.authState.value)
        assertTrue(h.session.signInRequired.value)
    }

    // ---- profile ----------------------------------------------------------

    @Test
    fun missingProfileIsNullNotAnError() = runTest {
        val h = Harness { respond("""{"error":"No profile"}""", HttpStatusCode.NotFound, jsonHeaders) }
        assertNull(h.api.profile())
    }

    @Test
    fun loadsSavedProfile() = runTest {
        val h = Harness {
            respond(
                """{"profile":{"goals":[]},"version":2,"updatedAt":"2026-10-09T10:00:00Z"}""",
                HttpStatusCode.OK, jsonHeaders
            )
        }
        val snapshot = assertNotNull(h.api.profile())
        assertEquals(2, snapshot.version)
        assertTrue("goals" in snapshot.profile)
    }

    @Test
    fun savesProfileAsJsonBody() = runTest {
        val h = Harness { respond("""{"status":"saved","version":3}""", HttpStatusCode.OK, jsonHeaders) }
        val profile: JsonObject = buildJsonObject { put("household", "x") }

        val result = h.api.saveProfile(profile)

        assertEquals(3, result.version)
        val request = h.requests.last()
        assertEquals(HttpMethod.Put, request.method)
        assertEquals("/profile", request.url.encodedPath)
        assertTrue(request.body.toByteArray().decodeToString().contains("\"household\""))
    }

    @Test
    fun surfacesTheBffErrorMessage() = runTest {
        val h = Harness {
            respond(
                """{"error":"The profile is missing required sections: goals."}""",
                HttpStatusCode.BadRequest, jsonHeaders
            )
        }
        val error = assertFailsWith<ApiException> { h.api.saveProfile(buildJsonObject { }) }
        assertEquals(400, error.status)
        assertTrue(error.message!!.contains("missing required sections"))
    }

    // ---- bootstrap --------------------------------------------------------

    @Test
    fun bootstrapReportsProfileAndLegalState() = runTest {
        val h = Harness { request ->
            when (request.url.encodedPath) {
                "/profile/status" -> respond("""{"saved":false,"version":0}""", HttpStatusCode.OK, jsonHeaders)
                else -> respond(
                    """{"allDocumentsAccepted":true,"intakeComplete":true,"questionnaireComplete":false,"betaOnboardingComplete":true}""",
                    HttpStatusCode.OK, jsonHeaders
                )
            }
        }
        val result = SessionBootstrap(h.api).run()

        assertEquals(false, result.profileSaved)
        assertEquals(false, result.legal?.questionnaireComplete)
        assertEquals(false, result.signedOut)
    }

    @Test
    fun bootstrapDetectsSignedOutAndToleratesFailures() = runTest {
        val unauthorized = Harness { respond("""{"error":"Sign in required."}""", HttpStatusCode.Unauthorized, jsonHeaders) }
        val signedOut = SessionBootstrap(unauthorized.api).run()
        assertTrue(signedOut.signedOut)
        assertNull(signedOut.profileSaved)

        val broken = Harness { respond("""{"error":"boom"}""", HttpStatusCode.BadGateway, jsonHeaders) }
        val failed = SessionBootstrap(broken.api).run()
        assertEquals(false, failed.signedOut)
        assertNull(failed.profileSaved)
        assertNull(failed.legal)
    }

    @Test
    fun bootstrapSurvivesANonExceptionThrowable() = runTest {
        // Browsers report a failed fetch as kotlin.Error, which `catch (e: Exception)` misses.
        val h = Harness { throw Error("Fail to fetch") }
        val result = SessionBootstrap(h.api).run()
        assertNull(result.profileSaved)
        assertNull(result.legal)
        assertEquals(false, result.signedOut)
    }

    // ---- session ----------------------------------------------------------

    @Test
    fun signingInClearsTheSignInRequirement() = runTest {
        val provider = FakeSignInProvider()
        val session = SessionManager(provider)
        session.onUnauthorized()
        assertTrue(session.signInRequired.value)

        assertEquals(SignInResult.Success, session.signIn())

        assertEquals(AuthState.SignedIn, session.authState.value)
        assertEquals(false, session.signInRequired.value)
        assertEquals("tok", session.accessToken())
    }

    @Test
    fun cancelledOrFailedSignInKeepsTheRequirement() = runTest {
        val provider = FakeSignInProvider(nextResult = SignInResult.Cancelled)
        val session = SessionManager(provider)
        session.onUnauthorized()

        assertEquals(SignInResult.Cancelled, session.signIn())
        assertTrue(session.signInRequired.value)
        assertEquals(AuthState.SignedOut, session.authState.value)

        provider.nextResult = SignInResult.Failed("nope")
        assertEquals(SignInResult.Failed("nope"), session.signIn())
        assertTrue(session.signInRequired.value)
    }

    @Test
    fun logoutEndsTheSessionAndAsksForSignIn() = runTest {
        val provider = FakeSignInProvider()
        val session = SessionManager(provider)
        session.signIn()

        session.signOut()

        assertEquals(1, provider.signOutCalls)
        assertEquals(AuthState.SignedOut, session.authState.value)
        assertTrue(session.signInRequired.value)
        assertNull(session.accessToken())
    }

    @Test
    fun noSignInProviderMeansNoSignInAndNoCrash() = runTest {
        val session = SessionManager(com.goldenboot.goldenbootfinancial.core.auth.UnavailableSignInProvider)
        assertEquals(false, session.canSignIn)
        assertNull(session.accessToken())
        assertTrue(session.signIn() is SignInResult.Failed)
        session.onUnauthorized()
        session.dismissSignIn() // demo/mock builds can continue without signing in
        assertEquals(false, session.signInRequired.value)
    }
}
