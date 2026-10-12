package com.goldenboot.goldenbootfinancial.core.foundation

import com.goldenboot.goldenbootfinancial.core.network.ensureSuccess
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject

/** Profile and legal-status calls shared by every feature (BFF: /profile, /profile/status, /legal/status). */
class FoundationApi(private val http: HttpClient) {

    suspend fun profileStatus(): ProfileStatus {
        val response = http.get("/profile/status")
        response.ensureSuccess()
        return response.body()
    }

    /** The saved profile, or null when the member has not saved one yet. */
    suspend fun profile(): ProfileSnapshot? {
        val response = http.get("/profile")
        if (response.status == HttpStatusCode.NotFound) return null
        response.ensureSuccess()
        return response.body()
    }

    suspend fun saveProfile(profile: JsonObject): ProfileSaveResult {
        val response = http.put("/profile") {
            contentType(ContentType.Application.Json)
            setBody(profile)
        }
        response.ensureSuccess()
        return response.body()
    }

    suspend fun legalStatus(): LegalStatus {
        val response = http.get("/legal/status")
        response.ensureSuccess()
        return response.body()
    }
}
