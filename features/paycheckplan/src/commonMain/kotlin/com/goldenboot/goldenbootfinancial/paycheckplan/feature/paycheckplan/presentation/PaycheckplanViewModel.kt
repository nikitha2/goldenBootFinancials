package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.model.PaycheckplanItem
import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.service.PaycheckplanService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for PaycheckPlan screen.
 *
 * Uses viewModelScope — all calls cancel when the ViewModel clears.
 * Duplicate call cancellation is handled by the network layer (CallDeduplicator).
 */
class PaycheckplanViewModel(
    private val service: PaycheckplanService
) : ViewModel() {
    private val _uiState = MutableStateFlow<PaycheckplanUiState>(PaycheckplanUiState.Loading)
    val uiState: StateFlow<PaycheckplanUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = PaycheckplanUiState.Loading
            try {
                val items = service.getAll()
                _uiState.value = if (items.isEmpty()) {
                    PaycheckplanUiState.Empty
                } else {
                    PaycheckplanUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = PaycheckplanUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadData()
    }
}

/**
 * UI states for PaycheckPlan screen.
 */
sealed interface PaycheckplanUiState {
    data object Loading : PaycheckplanUiState
    data object Empty : PaycheckplanUiState
    data class Success(val items: List<PaycheckplanItem>) : PaycheckplanUiState
    data class Error(val message: String) : PaycheckplanUiState
}
