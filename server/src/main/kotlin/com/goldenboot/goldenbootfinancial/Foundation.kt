package com.goldenboot.goldenbootfinancial

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

// ---- What the app receives (camelCase) --------------------------------------

/** The member's saved Golden Boot profile; [profile] is the free-form JSON the profile builder writes. */
@Serializable
data class ProfileEnvelope(
    val profile: JsonObject,
    val version: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ProfileStatus(val saved: Boolean, val version: Int, val updatedAt: String? = null)

@Serializable
data class ProfileSaveResult(val status: String, val version: Int, val updatedAt: String? = null)

/** The agreement / questionnaire gates the app uses to decide what to ask next. */
@Serializable
data class LegalStatus(
    val allDocumentsAccepted: Boolean,
    val intakeComplete: Boolean,
    val questionnaireComplete: Boolean,
    val betaOnboardingComplete: Boolean
)

// ---- What the upstream FastAPI sends (snake_case) ----------------------------

@Serializable
private data class UpstreamProfile(
    val profile: JsonObject,
    val version: Int,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
private data class UpstreamProfileStatus(
    val saved: Boolean,
    val version: Int,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
private data class UpstreamSaveResult(
    val status: String,
    val version: Int,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
private data class UpstreamLegalStatus(
    @SerialName("all_documents_accepted") val allDocumentsAccepted: Boolean,
    @SerialName("intake_complete") val intakeComplete: Boolean,
    @SerialName("questionnaire_complete") val questionnaireComplete: Boolean,
    @SerialName("beta_onboarding_complete") val betaOnboardingComplete: Boolean
)

/** In-memory profile for mock mode, so the app can save and reload end to end. */
class MockStore {
    @Volatile
    var profile: JsonObject? = null

    @Volatile
    var version: Int = 0

    @Volatile
    var updatedAt: String? = null
}

private val upstreamJson = Json { ignoreUnknownKeys = true; isLenient = true }

private val RequiredProfileSections = listOf("household", "cashFlow", "foundation", "accountPreferences", "goals", "risk")

/**
 * Foundation endpoints every screen depends on: profile and legal status.
 *
 * Upstream paths are the real FastAPI ones (`/profile`, `/profile/status`, `/legal/status`).
 */
fun Route.foundationRoutes(config: BffConfig, upstream: UpstreamClient, store: MockStore) {
    get("/profile/status") {
        if (!config.proxy) {
            return@get call.respond(ProfileStatus(store.profile != null, store.version, store.updatedAt))
        }
        call.viaUpstream(config) { auth, requestId ->
            val body = upstreamJson.decodeFromJsonElement(
                UpstreamProfileStatus.serializer(), upstream.getJson("/profile/status", auth, requestId)
            )
            ProfileStatus(body.saved, body.version, body.updatedAt)
        }
    }

    get("/profile") {
        if (!config.proxy) {
            val saved = store.profile ?: return@get call.respond(
                HttpStatusCode.NotFound, mapOf("error" to "No Golden Boot profile has been saved yet.")
            )
            return@get call.respond(ProfileEnvelope(saved, store.version, store.updatedAt, store.updatedAt))
        }
        call.viaUpstream(config) { auth, requestId ->
            val body = upstreamJson.decodeFromJsonElement(
                UpstreamProfile.serializer(), upstream.getJson("/profile", auth, requestId)
            )
            ProfileEnvelope(body.profile, body.version, body.createdAt, body.updatedAt)
        }
    }

    put("/profile") {
        val profile = call.receive<JsonObject>()
        if (!config.proxy) {
            val missing = RequiredProfileSections.filterNot { it in profile.keys }
            if (missing.isNotEmpty()) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "The profile is missing required sections: ${missing.sorted().joinToString(", ")}.")
                )
            }
            store.profile = profile
            store.version += 1
            store.updatedAt = java.time.Instant.now().toString()
            return@put call.respond(ProfileSaveResult("saved", store.version, store.updatedAt))
        }
        call.viaUpstream(config) { auth, requestId ->
            val body = upstreamJson.decodeFromJsonElement(
                UpstreamSaveResult.serializer(), upstream.putJson("/profile", profile, auth, requestId)
            )
            ProfileSaveResult(body.status, body.version, body.updatedAt)
        }
    }

    get("/legal/status") {
        if (!config.proxy) {
            return@get call.respond(
                LegalStatus(
                    allDocumentsAccepted = true,
                    intakeComplete = true,
                    questionnaireComplete = false,
                    betaOnboardingComplete = true
                )
            )
        }
        call.viaUpstream(config) { auth, requestId ->
            val body = upstreamJson.decodeFromJsonElement(
                UpstreamLegalStatus.serializer(), upstream.getJson("/legal/status", auth, requestId)
            )
            LegalStatus(
                body.allDocumentsAccepted, body.intakeComplete, body.questionnaireComplete, body.betaOnboardingComplete
            )
        }
    }
}

/**
 * Runs an upstream call for the signed-in member and responds with its result.
 *
 * Without an Authorization header the BFF answers 401 itself, unless anonymous calls are enabled.
 * Upstream error statuses (401/403/404/400...) are relayed; anything else becomes 502.
 */
private suspend inline fun <reified T : Any> ApplicationCall.viaUpstream(
    config: BffConfig,
    block: (authorization: String?, requestId: String?) -> T
) {
    val auth = request.headers["Authorization"]
    if (auth == null && !config.allowAnonymous) {
        return respond(HttpStatusCode.Unauthorized, mapOf("error" to "Sign in required."))
    }
    try {
        respond(block(auth, request.headers["X-Request-Id"]))
    } catch (error: Throwable) {
        respondUpstreamError(error)
    }
}
