package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.data.api

import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.data.dto.PaycheckplanDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * API client for PaycheckPlan network calls.
 */
class PaycheckplanApi(
    private val httpClient: HttpClient
) {
    // TODO: Update endpoint paths for your API
    suspend fun fetchAll(): List<PaycheckplanDto> {
        return httpClient.get("/paycheckplan").body()
    }

    suspend fun fetchById(id: String): PaycheckplanDto? {
        return httpClient.get("/paycheckplan/${id}").body()
    }
}
