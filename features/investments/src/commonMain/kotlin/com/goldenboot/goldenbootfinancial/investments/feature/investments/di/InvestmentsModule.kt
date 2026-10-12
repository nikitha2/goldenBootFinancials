package com.goldenboot.goldenbootfinancial.investments.feature.investments.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.goldenboot.goldenbootfinancial.investments.feature.investments.data.api.InvestmentsApi
import com.goldenboot.goldenbootfinancial.investments.feature.investments.data.repository.InvestmentsRepositoryImpl
import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.repository.InvestmentsRepository
import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.service.InvestmentsService
import com.goldenboot.goldenbootfinancial.investments.feature.investments.presentation.InvestmentsViewModel

/**
 * Koin module for Investments feature.
 *
 * Provides dependencies specific to the Investments module.
 */
val investmentsModule = module {
    // Data layer
    single { InvestmentsApi(get()) }
    single<InvestmentsRepository> { InvestmentsRepositoryImpl(get()) }

    // Domain layer
    single { InvestmentsService(get()) }

    // Presentation layer
    viewModel { InvestmentsViewModel(get()) }
}
