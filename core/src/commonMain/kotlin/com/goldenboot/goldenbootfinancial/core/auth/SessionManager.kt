package com.goldenboot.goldenbootfinancial.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Supplies the member's session token (a Clerk JWT) to the HTTP client. */
interface AuthTokenProvider {
    suspend fun accessToken(): String?
}

sealed interface AuthState {
    data object SignedOut : AuthState
    data object SignedIn : AuthState
}

/**
 * The app's view of the member's session, on top of a [SignInProvider].
 *
 * - [accessToken] hands the HTTP client a fresh token for every request.
 * - [signInRequired] turns true when the BFF rejects us (401) or the member logs out, and false
 *   again after a successful sign-in. The UI shows the sign-in screen while it is true. It stays
 *   false while the BFF is happy without a token (for example mock mode), so local development
 *   never bounces to a sign-in screen.
 */
class SessionManager(private val provider: SignInProvider) : AuthTokenProvider {
    private val _signInRequired = MutableStateFlow(false)
    val signInRequired: StateFlow<Boolean> = _signInRequired.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(authStateOf(provider.hasSession.value))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /** True when sign-in can actually be attempted on this build. */
    val canSignIn: Boolean get() = provider.isAvailable

    override suspend fun accessToken(): String? = provider.accessToken()

    suspend fun signIn(): SignInResult {
        val result = provider.signIn()
        if (result is SignInResult.Success) {
            _authState.value = AuthState.SignedIn
            _signInRequired.value = false
        }
        return result
    }

    /** Ends the session and asks for sign-in again. */
    suspend fun signOut() {
        provider.signOut()
        _authState.value = AuthState.SignedOut
        _signInRequired.value = true
    }

    /** The backend refused our token (or there was none): sign-in is needed. */
    fun onUnauthorized() {
        _authState.value = AuthState.SignedOut
        _signInRequired.value = true
    }

    /** "Continue without signing in" for builds that have no sign-in set up (demo/mock use). */
    fun dismissSignIn() {
        _signInRequired.value = false
    }

    private fun authStateOf(signedIn: Boolean): AuthState =
        if (signedIn) AuthState.SignedIn else AuthState.SignedOut
}
