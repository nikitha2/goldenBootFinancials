package com.goldenboot.goldenbootfinancial.profile.feature.profile.data.api

import com.goldenboot.goldenbootfinancial.profile.feature.profile.data.dto.ProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * API client for Profile network calls.
 */
class ProfileApi(
    private val httpClient: HttpClient
) {
    // TODO: Update endpoint paths for your API
    suspend fun fetchAll(): List<ProfileDto> {
        return httpClient.get("/profile-items").body()
    }

    suspend fun fetchById(id: String): ProfileDto? {
        return httpClient.get("/profile-items/${id}").body()
    }
}
