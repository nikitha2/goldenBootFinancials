package com.goldenboot.goldenbootfinancial

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** What the web app's rules engine says about a member's profile. */
sealed interface RecommendationOutcome {
    data class Ready(
        val wellnessScore: Int,
        val wellnessVersion: String?,
        val priority: String,
        /** hysa, employerRetirement, ira, brokerage, custodial, cd -> percent */
        val allocation: Map<String, Int>,
        val ruleVersion: String?,
        val monthlySavingsRate: Double?
    ) : RecommendationOutcome

    data class Unavailable(val message: String) : RecommendationOutcome
}

/** Live figures the rules engine may use instead of the member's self-reported ones. */
data class ConnectedFinancials(
    val liquidBalance: Double?,
    val investableBalance: Double?,
    val checkingBalance: Double?,
    val highestApr: Double?,
    val debtBalance: Double?
) {
    /** Missing values are omitted, never sent as 0: a literal 0 would override the reported balance. */
    fun toJson(): JsonObject = buildJsonObject {
        liquidBalance?.let { put("liquidBalance", it) }
        investableBalance?.let { put("investableBalance", it) }
        checkingBalance?.let { put("checkingBalance", it) }
        highestApr?.let { put("highestApr", it) }
        debtBalance?.let { put("debtBalance", it) }
    }
}

/**
 * Calls the web app's `POST /api/recommendation`, which holds the wellness-score, allocation and
 * paycheck-plan rules (GBF-WELL / GBF-ALLOC). The BFF reuses it instead of copying the rules, so
 * there is one implementation of them. The route is not behind login: it takes the profile in the body.
 */
class RecommendationClient(
    baseUrl: String,
    private val http: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true }) }
        install(HttpTimeout) { requestTimeoutMillis = 15_000 }
        expectSuccess = false
    }
) {
    private val url = baseUrl.trimEnd('/') + "/api/recommendation"

    suspend fun recommend(profile: JsonObject, connected: ConnectedFinancials): RecommendationOutcome {
        val payload = buildJsonObject {
            put("profile", profile)
            put("connected", connected.toJson())
        }
        return try {
            val response = http.post(url) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val body = response.body<JsonObject>()
            if (response.status.value !in 200..299) {
                RecommendationOutcome.Unavailable(body.string("error") ?: GENERIC_ERROR)
            } else {
                parse(body)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RecommendationOutcome.Unavailable(GENERIC_ERROR)
        }
    }

    private fun parse(body: JsonObject): RecommendationOutcome {
        val score = (body["wellnessScore"] as? JsonPrimitive)?.intOrNull
            ?: return RecommendationOutcome.Unavailable("Recommendation unavailable.")
        val allocation = (body["accountAllocation"] as? JsonObject)
            ?.mapNotNull { (key, value) -> (value as? JsonPrimitive)?.intOrNull?.let { key to it } }
            ?.toMap()
            .orEmpty()
        return RecommendationOutcome.Ready(
            wellnessScore = score,
            wellnessVersion = (body["wellness"] as? JsonObject)?.string("version"),
            priority = body.string("priority").orEmpty(),
            allocation = allocation,
            ruleVersion = body.string("ruleVersion"),
            monthlySavingsRate = ((body["metrics"] as? JsonObject)?.get("monthlySavingsRate") as? JsonPrimitive)?.doubleOrNull
        )
    }

    private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

    private companion object {
        const val GENERIC_ERROR = "Golden Boot could not generate your Game Plan."
    }
}
