package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.data.repository

import com.goldenboot.goldenbootfinancial.core.network.NetworkResult
import com.goldenboot.goldenbootfinancial.core.network.networkFetch
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.data.api.DashboardApi
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.DashboardSummary
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.repository.DashboardRepository

/**
 * Repository implementation for Dashboard.
 *
 * Fetches data from the network API with call deduplication.
 * Retries are handled by Ktor's HttpRequestRetry plugin (2 retries with exponential backoff).
 */
class DashboardRepositoryImpl(
    private val api: DashboardApi
) : DashboardRepository {
    override suspend fun getSummary(): DashboardSummary {
        return when (val result = networkFetch { api.fetchSummary() }) {
            is NetworkResult.Success -> result.data.toDomain()
            is NetworkResult.Error -> throw result.exception
        }
    }
}
