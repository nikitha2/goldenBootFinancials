package com.goldenboot.goldenbootfinancial.ui.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.core.auth.SessionManager
import com.goldenboot.goldenbootfinancial.core.auth.SignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SignInUiState {
    data object Idle : SignInUiState
    data object SigningIn : SignInUiState
    data class Error(val message: String) : SignInUiState
}

/**
 * Drives the sign-in screen. Success is not handled here: the session's "sign-in required" flag
 * clears and the app navigates on from that, so every way of getting signed in behaves the same.
 */
class SignInViewModel(private val session: SessionManager) : ViewModel() {
    private val _state = MutableStateFlow<SignInUiState>(SignInUiState.Idle)
    val state: StateFlow<SignInUiState> = _state.asStateFlow()

    /** False when this build has no sign-in set up (no Clerk key). */
    val canSignIn: Boolean get() = session.canSignIn

    fun signIn() {
        if (_state.value is SignInUiState.SigningIn) return
        _state.value = SignInUiState.SigningIn
        viewModelScope.launch {
            _state.value = when (val result = session.signIn()) {
                SignInResult.Success, SignInResult.Cancelled -> SignInUiState.Idle
                is SignInResult.Failed -> SignInUiState.Error(result.message)
            }
        }
    }

    /** For builds without sign-in (demo / mock BFF): carry on without a session. */
    fun continueInDemoMode() = session.dismissSignIn()
}
