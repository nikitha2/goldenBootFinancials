package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.model.CashflowItem
import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.service.CashflowService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for CashFlow screen.
 *
 * Uses viewModelScope — all calls cancel when the ViewModel clears.
 * Duplicate call cancellation is handled by the network layer (CallDeduplicator).
 */
class CashflowViewModel(
    private val service: CashflowService
) : ViewModel() {
    private val _uiState = MutableStateFlow<CashflowUiState>(CashflowUiState.Loading)
    val uiState: StateFlow<CashflowUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = CashflowUiState.Loading
            try {
                val items = service.getAll()
                _uiState.value = if (items.isEmpty()) {
                    CashflowUiState.Empty
                } else {
                    CashflowUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = CashflowUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

/**
 * UI states for CashFlow screen.
 */
sealed interface CashflowUiState {
    data object Loading : CashflowUiState
    data object Empty : CashflowUiState
    data class Success(val items: List<CashflowItem>) : CashflowUiState
    data class Error(val message: String) : CashflowUiState
}
