package com.goldenboot.goldenbootfinancial.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.core.foundation.BootstrapResult
import com.goldenboot.goldenbootfinancial.core.foundation.SessionBootstrap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Splash Screen.
 *
 * Handles initialization tasks such as:
 * - Loading user preferences
 * - Checking authentication status
 * - Preparing app data
 */
class SplashViewModel(
    private val sessionBootstrap: SessionBootstrap
) : ViewModel() {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _bootstrap = MutableStateFlow<BootstrapResult?>(null)

    /** Launch-time checks (signed in? profile saved? agreements done?); null if they did not finish. */
    val bootstrap: StateFlow<BootstrapResult?> = _bootstrap.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            // Check the member's session while the splash is on screen. A slow or unreachable
            // BFF must not trap the app on the splash, so give up after a few seconds.
            val checks = async {
                try {
                    withTimeoutOrNull(BOOTSTRAP_TIMEOUT_MS) { sessionBootstrap.run() }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    null // unreachable BFF: carry on, the screens show their own errors
                }
            }

            delay(2000) // Minimum splash display time

            _bootstrap.value = checks.await()

            _isReady.value = true
        }
    }

    private companion object {
        const val BOOTSTRAP_TIMEOUT_MS = 5_000L
    }
}
