package com.goldenboot.goldenbootfinancial.investments.feature.investments.data.repository

import com.goldenboot.goldenbootfinancial.core.network.NetworkResult
import com.goldenboot.goldenbootfinancial.core.network.networkFetch
import com.goldenboot.goldenbootfinancial.investments.feature.investments.data.api.InvestmentsApi
import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.model.InvestmentsItem
import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.repository.InvestmentsRepository

/**
 * Repository implementation for Investments.
 *
 * Fetches data from the network API with call deduplication.
 * Retries are handled by Ktor's HttpRequestRetry plugin (2 retries with exponential backoff).
 */
class InvestmentsRepositoryImpl(
    private val api: InvestmentsApi
) : InvestmentsRepository {
    override suspend fun getAll(): List<InvestmentsItem> {
        return when (val result = networkFetch { api.fetchAll() }) {
            is NetworkResult.Success -> result.data.map { it.toDomain() }
            is NetworkResult.Error -> throw result.exception
        }
    }

    override suspend fun getById(id: String): InvestmentsItem? {
        return when (val result = networkFetch { api.fetchById(id) }) {
            is NetworkResult.Success -> result.data?.toDomain()
            is NetworkResult.Error -> throw result.exception
        }
    }
}
