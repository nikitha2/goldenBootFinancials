package com.goldenboot.goldenbootfinancial.di

import com.goldenboot.goldenbootfinancial.core.config.ApiConfig
import com.goldenboot.goldenbootfinancial.core.config.AppInfo
import com.goldenboot.goldenbootfinancial.core.di.NAMED_BASE_URL
import com.goldenboot.goldenbootfinancial.core.di.coreModule
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.di.dashboardModule
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.di.cashflowModule
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.di.payrollModule
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.di.paycheckplanModule
import com.goldenboot.goldenbootfinancial.investments.feature.investments.di.investmentsModule
import com.goldenboot.goldenbootfinancial.profile.feature.profile.di.profileModule
import com.goldenboot.goldenbootfinancial.ui.signin.SignInViewModel
import com.goldenboot.goldenbootfinancial.ui.splash.SplashViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    viewModel { SplashViewModel(get()) }
    viewModel { SignInViewModel(get()) }
}

/**
 * Initializes Koin dependency injection framework.
 *
 * This function should be called once at app startup from platform-specific entry points
 * (MainApplication on Android, MainViewController on iOS).
 *
 * @param appInfo Build identity (version + environment) supplied by the platform entry point. The
 *                API base URL is derived from it here, once, for every platform.
 * @param platformModules Additional platform-specific modules, only for things that truly differ
 *                        per platform. Loaded last so they can override common bindings.
 * @param platformConfig Platform-specific Koin configuration (e.g., androidContext).
 */
fun initKoin(
    appInfo: AppInfo = AppInfo(),
    platformModules: List<Module> = emptyList(),
    platformConfig: (KoinApplication.() -> Unit)? = null
) {
    val appInfoModule = module {
        single { appInfo }
        single<String>(named(NAMED_BASE_URL)) { ApiConfig.baseUrlFor(appInfo.environment) }
    }

    startKoin {
        platformConfig?.invoke(this)
        modules(
            appInfoModule,
            appModule,
            coreModule,
            dashboardModule,
            cashflowModule,
            payrollModule,
            paycheckplanModule,
            investmentsModule,
            profileModule
        )
        modules(platformModules)
    }
}
