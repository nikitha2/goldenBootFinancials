package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.repository

import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.DashboardSummary

/** Repository contract for Dashboard data access. */
interface DashboardRepository {
    suspend fun getSummary(): DashboardSummary
}
