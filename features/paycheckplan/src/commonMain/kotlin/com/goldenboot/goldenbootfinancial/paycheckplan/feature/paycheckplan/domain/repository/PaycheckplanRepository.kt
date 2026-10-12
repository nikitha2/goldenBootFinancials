package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.repository

import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.model.PaycheckplanItem

/**
 * Repository contract for PaycheckPlan data access.
 */
interface PaycheckplanRepository {
    suspend fun getAll(): List<PaycheckplanItem>
    suspend fun getById(id: String): PaycheckplanItem?
}
