package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.service

import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.model.CashflowItem
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.repository.CashflowRepository

/**
 * Service layer for CashFlow business logic.
 */
class CashflowService(
    private val repository: CashflowRepository
) {
    suspend fun getAll(): List<CashflowItem> {
        return repository.getAll()
    }

    suspend fun getById(id: String): CashflowItem? {
        return repository.getById(id)
    }
}
