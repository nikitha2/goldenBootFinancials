package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.data.api

import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.data.dto.PayrollDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * API client for Payroll network calls.
 */
class PayrollApi(
    private val httpClient: HttpClient
) {
    // TODO: Update endpoint paths for your API
    suspend fun fetchAll(): List<PayrollDto> {
        return httpClient.get("/payroll").body()
    }

    suspend fun fetchById(id: String): PayrollDto? {
        return httpClient.get("/payroll/${id}").body()
    }
}
