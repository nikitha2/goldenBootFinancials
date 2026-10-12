package com.goldenboot.goldenbootfinancial.core.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.goldenboot.goldenbootfinancial.core.navigation.NavigationManager
import com.goldenboot.goldenbootfinancial.core.auth.SessionManager
import com.goldenboot.goldenbootfinancial.core.auth.SignInProvider
import com.goldenboot.goldenbootfinancial.core.auth.UnavailableSignInProvider
import com.goldenboot.goldenbootfinancial.core.foundation.FoundationApi
import com.goldenboot.goldenbootfinancial.core.foundation.SessionBootstrap
import com.goldenboot.goldenbootfinancial.core.network.BearerAuth
import com.goldenboot.goldenbootfinancial.core.network.DeduplicatedRequests

/**
 * Named qualifier for the API base URL. Each platform's entry point
 * (MainApplication on Android, MainViewController on iOS, main() on
 * Desktop/Web) binds a String singleton with this qualifier so the shared
 * HttpClient picks up the right base URL per platform.
 */
const val NAMED_BASE_URL = "baseUrl"

/**
 * Core Koin module providing common dependencies across the application.
 *
 * This module is included in all build variants and provides:
 * - HTTP client for networking (with retry + call deduplication + base URL)
 * - Navigation management
 * - Common utilities
 */
val coreModule = module {
    // Session. Platforms that can sign in override SignInProvider (see platformModules).
    single<SignInProvider> { UnavailableSignInProvider }
    single { SessionManager(get()) }

    // Networking
    single {
        val baseUrl = getOrNull<String>(named(NAMED_BASE_URL)).orEmpty()
        val session = get<SessionManager>()
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }

            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 2)
                retryOnException(maxRetries = 2, retryOnTimeout = true)
                exponentialDelay()
            }

            install(DeduplicatedRequests)

            install(BearerAuth) {
                tokenProvider = session
                onUnauthorized = session::onUnauthorized
            }

            if (baseUrl.isNotBlank()) {
                defaultRequest {
                    url(baseUrl)
                }
            }
        }
    }

    // Foundation: profile + legal status, and the launch-time bootstrap
    single { FoundationApi(get()) }
    single { SessionBootstrap(get()) }

    // Navigation
    single { NavigationManager() }
}
