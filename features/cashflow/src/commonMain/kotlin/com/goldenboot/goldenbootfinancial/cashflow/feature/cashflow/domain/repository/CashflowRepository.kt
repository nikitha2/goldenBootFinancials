package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.repository

import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.model.CashflowItem

/**
 * Repository contract for CashFlow data access.
 */
interface CashflowRepository {
    suspend fun getAll(): List<CashflowItem>
    suspend fun getById(id: String): CashflowItem?
}
