package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.data.api

import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.data.dto.CashflowDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * API client for CashFlow network calls.
 */
class CashflowApi(
    private val httpClient: HttpClient
) {
    // TODO: Update endpoint paths for your API
    suspend fun fetchAll(): List<CashflowDto> {
        return httpClient.get("/cashflow").body()
    }

    suspend fun fetchById(id: String): CashflowDto? {
        return httpClient.get("/cashflow/${id}").body()
    }
}
