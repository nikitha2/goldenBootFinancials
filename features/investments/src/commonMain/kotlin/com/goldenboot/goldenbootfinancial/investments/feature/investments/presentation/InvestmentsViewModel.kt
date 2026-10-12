package com.goldenboot.goldenbootfinancial.investments.feature.investments.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.model.InvestmentsItem
import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.service.InvestmentsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Investments screen.
 *
 * Uses viewModelScope — all calls cancel when the ViewModel clears.
 * Duplicate call cancellation is handled by the network layer (CallDeduplicator).
 */
class InvestmentsViewModel(
    private val service: InvestmentsService
) : ViewModel() {
    private val _uiState = MutableStateFlow<InvestmentsUiState>(InvestmentsUiState.Loading)
    val uiState: StateFlow<InvestmentsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = InvestmentsUiState.Loading
            try {
                val items = service.getAll()
                _uiState.value = if (items.isEmpty()) {
                    InvestmentsUiState.Empty
                } else {
                    InvestmentsUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = InvestmentsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

/**
 * UI states for Investments screen.
 */
sealed interface InvestmentsUiState {
    data object Loading : InvestmentsUiState
    data object Empty : InvestmentsUiState
    data class Success(val items: List<InvestmentsItem>) : InvestmentsUiState
    data class Error(val message: String) : InvestmentsUiState
}
