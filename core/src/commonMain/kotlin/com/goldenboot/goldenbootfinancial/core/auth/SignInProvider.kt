package com.goldenboot.goldenbootfinancial.core.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** How a sign-in attempt ended. */
sealed interface SignInResult {
    data object Success : SignInResult

    /** The member closed the sign-in page without finishing. Not an error. */
    data object Cancelled : SignInResult

    data class Failed(val message: String) : SignInResult
}

/**
 * Signs the member in and supplies session tokens (Clerk, in production).
 *
 * The app only talks to this interface, so sign-in is written once in shared code and a platform
 * supplies just the part that truly differs: opening Clerk's sign-in page. The hosted page handles
 * Google, the authenticator-code step and MFA enrollment, so none of that is rebuilt here.
 */
interface SignInProvider {
    /** False when this platform or build has no sign-in configured (for example, no Clerk key). */
    val isAvailable: Boolean

    /** Whether a session currently exists. */
    val hasSession: StateFlow<Boolean>

    /** Opens the sign-in page and suspends until the member finishes, cancels or it fails. */
    suspend fun signIn(): SignInResult

    /** A fresh session token for the backend, or null when signed out. */
    suspend fun accessToken(): String?

    suspend fun signOut()
}

/** Used where sign-in is not configured: every call reports that, and nothing throws. */
object UnavailableSignInProvider : SignInProvider {
    override val isAvailable: Boolean = false
    override val hasSession: StateFlow<Boolean> = MutableStateFlow(false)
    override suspend fun signIn(): SignInResult = SignInResult.Failed("Sign-in is not set up in this build.")
    override suspend fun accessToken(): String? = null
    override suspend fun signOut() = Unit
}
