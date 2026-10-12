package com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.service

import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.model.InvestmentsItem
import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.repository.InvestmentsRepository

/**
 * Service layer for Investments business logic.
 */
class InvestmentsService(
    private val repository: InvestmentsRepository
) {
    suspend fun getAll(): List<InvestmentsItem> {
        return repository.getAll()
    }

    suspend fun getById(id: String): InvestmentsItem? {
        return repository.getById(id)
    }
}
