package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.data.api.PaycheckplanApi
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.data.repository.PaycheckplanRepositoryImpl
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.repository.PaycheckplanRepository
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.service.PaycheckplanService
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.presentation.PaycheckplanViewModel

/**
 * Koin module for PaycheckPlan feature.
 *
 * Provides dependencies specific to the PaycheckPlan module.
 */
val paycheckplanModule = module {
    // Data layer
    single { PaycheckplanApi(get()) }
    single<PaycheckplanRepository> { PaycheckplanRepositoryImpl(get()) }

    // Domain layer
    single { PaycheckplanService(get()) }

    // Presentation layer
    viewModel { PaycheckplanViewModel(get()) }
}
