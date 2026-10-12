package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.data.repository

import com.goldenboot.goldenbootfinancial.core.network.NetworkResult
import com.goldenboot.goldenbootfinancial.core.network.networkFetch
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.data.api.PayrollApi
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.model.PayrollItem
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.repository.PayrollRepository

/**
 * Repository implementation for Payroll.
 *
 * Fetches data from the network API with call deduplication.
 * Retries are handled by Ktor's HttpRequestRetry plugin (2 retries with exponential backoff).
 */
class PayrollRepositoryImpl(
    private val api: PayrollApi
) : PayrollRepository {
    override suspend fun getAll(): List<PayrollItem> {
        return when (val result = networkFetch { api.fetchAll() }) {
            is NetworkResult.Success -> result.data.map { it.toDomain() }
            is NetworkResult.Error -> throw result.exception
        }
    }

    override suspend fun getById(id: String): PayrollItem? {
        return when (val result = networkFetch { api.fetchById(id) }) {
            is NetworkResult.Success -> result.data?.toDomain()
            is NetworkResult.Error -> throw result.exception
        }
    }
}
