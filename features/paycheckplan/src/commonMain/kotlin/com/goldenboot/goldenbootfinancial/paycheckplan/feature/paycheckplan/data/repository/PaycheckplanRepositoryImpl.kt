package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.data.repository

import com.goldenboot.goldenbootfinancial.core.network.NetworkResult
import com.goldenboot.goldenbootfinancial.core.network.networkFetch
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.data.api.PaycheckplanApi
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.model.PaycheckplanItem
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.repository.PaycheckplanRepository

/**
 * Repository implementation for PaycheckPlan.
 *
 * Fetches data from the network API with call deduplication.
 * Retries are handled by Ktor's HttpRequestRetry plugin (2 retries with exponential backoff).
 */
class PaycheckplanRepositoryImpl(
    private val api: PaycheckplanApi
) : PaycheckplanRepository {
    override suspend fun getAll(): List<PaycheckplanItem> {
        return when (val result = networkFetch { api.fetchAll() }) {
            is NetworkResult.Success -> result.data.map { it.toDomain() }
            is NetworkResult.Error -> throw result.exception
        }
    }

    override suspend fun getById(id: String): PaycheckplanItem? {
        return when (val result = networkFetch { api.fetchById(id) }) {
            is NetworkResult.Success -> result.data?.toDomain()
            is NetworkResult.Error -> throw result.exception
        }
    }
}
