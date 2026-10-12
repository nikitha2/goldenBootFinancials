package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.model.PayrollItem
import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.service.PayrollService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Payroll screen.
 *
 * Uses viewModelScope — all calls cancel when the ViewModel clears.
 * Duplicate call cancellation is handled by the network layer (CallDeduplicator).
 */
class PayrollViewModel(
    private val service: PayrollService
) : ViewModel() {
    private val _uiState = MutableStateFlow<PayrollUiState>(PayrollUiState.Loading)
    val uiState: StateFlow<PayrollUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = PayrollUiState.Loading
            try {
                val items = service.getAll()
                _uiState.value = if (items.isEmpty()) {
                    PayrollUiState.Empty
                } else {
                    PayrollUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = PayrollUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

/**
 * UI states for Payroll screen.
 */
sealed interface PayrollUiState {
    data object Loading : PayrollUiState
    data object Empty : PayrollUiState
    data class Success(val items: List<PayrollItem>) : PayrollUiState
    data class Error(val message: String) : PayrollUiState
}
