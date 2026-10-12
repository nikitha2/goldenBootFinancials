package com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.repository

import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.model.InvestmentsItem

/**
 * Repository contract for Investments data access.
 */
interface InvestmentsRepository {
    suspend fun getAll(): List<InvestmentsItem>
    suspend fun getById(id: String): InvestmentsItem?
}
