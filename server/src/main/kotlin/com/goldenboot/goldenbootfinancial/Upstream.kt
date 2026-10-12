package com.goldenboot.goldenbootfinancial

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/** Thrown when the upstream API answers with a non-2xx status; the BFF relays it. */
class UpstreamException(val status: HttpStatusCode, message: String) : Exception(message)

/**
 * Client for the upstream Golden Boot API (FastAPI).
 *
 * The caller's `Authorization` header (a Clerk session token) is forwarded
 * untouched, so the BFF never stores or mints tokens itself. [authorization] may
 * be null only when the BFF is configured to allow anonymous calls, which pairs
 * with the upstream's local demo-auth switch.
 */
class UpstreamClient(
    baseUrl: String,
    private val http: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true }) }
        expectSuccess = false
    }
) {
    private val base = baseUrl.trimEnd('/')

    suspend fun getJson(path: String, authorization: String?, requestId: String?): JsonElement =
        check(
            "$base$path",
            http.get("$base$path") {
                authorization?.let { header(HttpHeaders.Authorization, it) }
                requestId?.let { header("X-Request-Id", it) }
            }
        ).body()

    suspend fun postJson(path: String, body: JsonElement, authorization: String?, requestId: String?): JsonElement =
        check(
            "$base$path",
            http.post("$base$path") {
                authorization?.let { header(HttpHeaders.Authorization, it) }
                requestId?.let { header("X-Request-Id", it) }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        ).body()

    suspend fun putJson(path: String, body: JsonElement, authorization: String?, requestId: String?): JsonElement =
        check(
            "$base$path",
            http.put("$base$path") {
                authorization?.let { header(HttpHeaders.Authorization, it) }
                requestId?.let { header("X-Request-Id", it) }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        ).body()

    /** Passes 2xx through; turns anything else into an [UpstreamException] carrying FastAPI's `detail`. */
    private suspend fun check(url: String, response: HttpResponse): HttpResponse {
        if (response.status.value in 200..299) return response
        val detail = runCatching {
            (Json.parseToJsonElement(response.bodyAsText()).jsonObject["detail"] as? JsonPrimitive)?.content
        }.getOrNull()
        throw UpstreamException(response.status, detail ?: "Upstream ${response.status} for $url")
    }

    fun close() = http.close()
}

/** BFF runtime settings, read from environment variables. */
data class BffConfig(
    val upstreamBaseUrl: String = System.getenv("UPSTREAM_BASE_URL") ?: "https://api.goldenboot.com",
    /** `mock` serves MockData (default, for local dev); `proxy` calls the upstream API. */
    val mode: String = (System.getenv("BFF_MODE") ?: "mock").lowercase(),
    /** The Golden Boot web app, whose `/api/recommendation` route owns the Game Plan and wellness rules. */
    val webAppBaseUrl: String = System.getenv("WEB_APP_BASE_URL") ?: "https://app.goldenbootfinancial.com",
    /** Forward calls with no Authorization header. Only for an upstream running its demo-auth switch. */
    val allowAnonymous: Boolean = System.getenv("BFF_ALLOW_ANONYMOUS").equals("true", ignoreCase = true)
) {
    val proxy: Boolean get() = mode == "proxy"
}
