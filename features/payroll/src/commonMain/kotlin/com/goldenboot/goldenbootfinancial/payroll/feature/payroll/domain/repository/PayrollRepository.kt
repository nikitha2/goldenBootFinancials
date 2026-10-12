package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.repository

import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.model.PayrollItem

/**
 * Repository contract for Payroll data access.
 */
interface PayrollRepository {
    suspend fun getAll(): List<PayrollItem>
    suspend fun getById(id: String): PayrollItem?
}
