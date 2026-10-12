package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.service

import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.model.PaycheckplanItem
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.repository.PaycheckplanRepository

/**
 * Service layer for PaycheckPlan business logic.
 */
class PaycheckplanService(
    private val repository: PaycheckplanRepository
) {
    suspend fun getAll(): List<PaycheckplanItem> {
        return repository.getAll()
    }

    suspend fun getById(id: String): PaycheckplanItem? {
        return repository.getById(id)
    }
}
