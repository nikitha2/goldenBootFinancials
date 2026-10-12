package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.service

import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.model.PayrollItem
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.repository.PayrollRepository

/**
 * Service layer for Payroll business logic.
 */
class PayrollService(
    private val repository: PayrollRepository
) {
    suspend fun getAll(): List<PayrollItem> {
        return repository.getAll()
    }

    suspend fun getById(id: String): PayrollItem? {
        return repository.getById(id)
    }
}
