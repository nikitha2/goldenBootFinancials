package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.service

import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.DashboardSummary
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.repository.DashboardRepository

/** Service layer for Dashboard business logic. */
class DashboardService(
    private val repository: DashboardRepository
) {
    suspend fun getSummary(): DashboardSummary = repository.getSummary()
}
