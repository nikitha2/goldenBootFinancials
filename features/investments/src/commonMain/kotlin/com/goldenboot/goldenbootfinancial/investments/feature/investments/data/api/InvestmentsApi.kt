package com.goldenboot.goldenbootfinancial.investments.feature.investments.data.api

import com.goldenboot.goldenbootfinancial.investments.feature.investments.data.dto.InvestmentsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * API client for Investments network calls.
 */
class InvestmentsApi(
    private val httpClient: HttpClient
) {
    // TODO: Update endpoint paths for your API
    suspend fun fetchAll(): List<InvestmentsDto> {
        return httpClient.get("/investments").body()
    }

    suspend fun fetchById(id: String): InvestmentsDto? {
        return httpClient.get("/investments/${id}").body()
    }
}
