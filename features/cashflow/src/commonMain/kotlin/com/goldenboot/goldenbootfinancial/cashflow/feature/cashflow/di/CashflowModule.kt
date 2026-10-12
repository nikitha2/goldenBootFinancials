package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.data.api.CashflowApi
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.data.repository.CashflowRepositoryImpl
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.repository.CashflowRepository
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.service.CashflowService
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.presentation.CashflowViewModel

/**
 * Koin module for CashFlow feature.
 *
 * Provides dependencies specific to the CashFlow module.
 */
val cashflowModule = module {
    // Data layer
    single { CashflowApi(get()) }
    single<CashflowRepository> { CashflowRepositoryImpl(get()) }

    // Domain layer
    single { CashflowService(get()) }

    // Presentation layer
    viewModel { CashflowViewModel(get()) }
}
