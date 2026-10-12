package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.data.api

import com.goldenboot.goldenbootfinancial.core.network.ensureSuccess
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.data.dto.DashboardSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** API client for the Dashboard BFF endpoint. */
class DashboardApi(
    private val httpClient: HttpClient
) {
    /**
     * The whole Dashboard in one call. The member's local day is sent so today's net-worth and
     * wellness readings are recorded against the right calendar date.
     */
    suspend fun fetchSummary(): DashboardSummaryDto {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        val response = httpClient.get("/dashboard") { parameter("date", today) }
        response.ensureSuccess()
        return response.body()
    }
}
