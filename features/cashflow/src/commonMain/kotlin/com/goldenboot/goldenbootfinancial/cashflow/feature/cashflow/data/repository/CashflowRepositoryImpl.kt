package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.data.repository

import com.goldenboot.goldenbootfinancial.core.network.NetworkResult
import com.goldenboot.goldenbootfinancial.core.network.networkFetch
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.data.api.CashflowApi
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.model.CashflowItem
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.repository.CashflowRepository

/**
 * Repository implementation for CashFlow.
 *
 * Fetches data from the network API with call deduplication.
 * Retries are handled by Ktor's HttpRequestRetry plugin (2 retries with exponential backoff).
 */
class CashflowRepositoryImpl(
    private val api: CashflowApi
) : CashflowRepository {
    override suspend fun getAll(): List<CashflowItem> {
        return when (val result = networkFetch { api.fetchAll() }) {
            is NetworkResult.Success -> result.data.map { it.toDomain() }
            is NetworkResult.Error -> throw result.exception
        }
    }

    override suspend fun getById(id: String): CashflowItem? {
        return when (val result = networkFetch { api.fetchById(id) }) {
            is NetworkResult.Success -> result.data?.toDomain()
            is NetworkResult.Error -> throw result.exception
        }
    }
}
