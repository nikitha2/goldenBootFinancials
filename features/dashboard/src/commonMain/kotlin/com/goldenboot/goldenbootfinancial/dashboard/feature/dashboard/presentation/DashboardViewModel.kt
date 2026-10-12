package com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.model.DashboardSummary
import com.goldenboot.goldenbootfinancial.dashboard.feature.dashboard.domain.service.DashboardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Dashboard screen.
 *
 * Uses viewModelScope — all calls cancel when the ViewModel clears.
 * Duplicate call cancellation is handled by the network layer (CallDeduplicator).
 */
class DashboardViewModel(
    private val service: DashboardService
) : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                _uiState.value = DashboardUiState.Success(service.getSummary())
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

/**
 * UI states for Dashboard screen.
 */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(val summary: DashboardSummary) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
