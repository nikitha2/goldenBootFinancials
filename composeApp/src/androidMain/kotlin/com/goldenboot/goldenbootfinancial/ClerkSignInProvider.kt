package com.goldenboot.goldenbootfinancial

import android.content.Context
import android.util.Log
import com.clerk.api.Clerk
import com.clerk.api.hostedauth.HostedAuthCancellationException
import com.clerk.api.network.serialization.ClerkResult
import com.goldenboot.goldenbootfinancial.core.auth.SignInProvider
import com.goldenboot.goldenbootfinancial.core.auth.SignInResult
import com.goldenboot.goldenbootfinancial.core.auth.UnavailableSignInProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Android sign-in through Clerk's hosted page.
 *
 * `startHostedAuth()` opens Clerk's Account Portal in the system browser (with PKCE) and activates
 * the session it creates. The page offers whatever the Clerk instance is configured for (Google) and
 * runs the authenticator-code and MFA-enrollment steps itself, so none of that is rebuilt here.
 * The session token it yields is the same Clerk JWT the web app sends, which the backend verifies.
 */
class ClerkSignInProvider private constructor() : SignInProvider {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val isAvailable: Boolean = true

    override val hasSession: StateFlow<Boolean> =
        Clerk.sessionFlow.map { it != null }.stateIn(scope, SharingStarted.Eagerly, Clerk.session != null)

    override suspend fun signIn(): SignInResult {
        if (!awaitReady()) return SignInResult.Failed("Sign-in is still starting. Please try again.")
        return try {
            when (val result = Clerk.auth.startHostedAuth()) {
                is ClerkResult.Success -> SignInResult.Success
                is ClerkResult.Failure ->
                    if (result.throwable is HostedAuthCancellationException) SignInResult.Cancelled
                    else SignInResult.Failed(
                        result.error?.errors?.firstNotNullOfOrNull { it.message }
                            ?: result.throwable?.message
                            ?: "Sign-in did not finish. Please try again."
                    )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            SignInResult.Failed(e.message ?: "Sign-in did not finish. Please try again.")
        }
    }

    override suspend fun accessToken(): String? {
        if (!awaitReady() || Clerk.session == null) return null
        return (Clerk.auth.getToken() as? ClerkResult.Success)?.value
    }

    override suspend fun signOut() {
        if (awaitReady()) Clerk.auth.signOut()
    }

    private suspend fun awaitReady(): Boolean =
        withTimeoutOrNull(READY_TIMEOUT_MS) { Clerk.isInitialized.first { it } } == true

    companion object {
        private const val TAG = "ClerkSignIn"
        private const val READY_TIMEOUT_MS = 4_000L

        /**
         * A Clerk-backed provider, or [UnavailableSignInProvider] when there is no (or an invalid)
         * publishable key, so a missing key never crashes the app.
         */
        fun create(context: Context, publishableKey: String): SignInProvider {
            if (publishableKey.isBlank()) return UnavailableSignInProvider
            return try {
                Clerk.initialize(context, publishableKey)
                ClerkSignInProvider()
            } catch (e: Exception) {
                Log.w(TAG, "Clerk could not be initialised; sign-in is disabled: ${e.message}")
                UnavailableSignInProvider
            }
        }
    }
}
