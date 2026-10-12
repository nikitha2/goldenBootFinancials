package com.goldenboot.goldenbootfinancial

import android.app.Application
import com.goldenboot.goldenbootfinancial.core.auth.SignInProvider
import com.goldenboot.goldenbootfinancial.core.config.AppInfo
import com.goldenboot.goldenbootfinancial.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // The one thing Android supplies for sign-in: how to open Clerk's sign-in page.
        val signInProvider = ClerkSignInProvider.create(this, BuildConfig.CLERK_PUBLISHABLE_KEY)

        initKoin(
            appInfo = AppInfo(
                version = BuildConfig.VERSION_NAME.substringBefore('-'),
                environment = BuildConfig.ENVIRONMENT
            ),
            platformModules = listOf(module { single<SignInProvider> { signInProvider } })
        ) {
            androidLogger(Level.ERROR)
            androidContext(this@MainApplication)
        }
    }
}
