package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.data.api.DashboardApi
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.repository.DashboardRepository
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.service.DashboardService
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.presentation.DashboardViewModel

/**
 * Koin module for Dashboard feature.
 *
 * Provides dependencies specific to the Dashboard module.
 */
val dashboardModule = module {
    // Data layer
    single { DashboardApi(get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }

    // Domain layer
    single { DashboardService(get()) }

    // Presentation layer
    viewModel { DashboardViewModel(get()) }
}
