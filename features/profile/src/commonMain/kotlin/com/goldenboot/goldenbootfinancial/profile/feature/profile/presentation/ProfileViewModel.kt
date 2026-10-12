package com.goldenboot.goldenbootfinancial.profile.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.model.ProfileItem
import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.service.ProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Profile screen.
 *
 * Uses viewModelScope — all calls cancel when the ViewModel clears.
 * Duplicate call cancellation is handled by the network layer (CallDeduplicator).
 */
class ProfileViewModel(
    private val service: ProfileService
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val items = service.getAll()
                _uiState.value = if (items.isEmpty()) {
                    ProfileUiState.Empty
                } else {
                    ProfileUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

/**
 * UI states for Profile screen.
 */
sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Empty : ProfileUiState
    data class Success(val items: List<ProfileItem>) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
