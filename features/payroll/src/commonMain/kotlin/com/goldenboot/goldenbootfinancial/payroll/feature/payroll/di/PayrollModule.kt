package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.data.api.PayrollApi
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.data.repository.PayrollRepositoryImpl
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.repository.PayrollRepository
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.service.PayrollService
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.presentation.PayrollViewModel

/**
 * Koin module for Payroll feature.
 *
 * Provides dependencies specific to the Payroll module.
 */
val payrollModule = module {
    // Data layer
    single { PayrollApi(get()) }
    single<PayrollRepository> { PayrollRepositoryImpl(get()) }

    // Domain layer
    single { PayrollService(get()) }

    // Presentation layer
    viewModel { PayrollViewModel(get()) }
}
