package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.model

/**
 * Domain model for CashFlow.
 */
data class CashflowItem(
    val id: String,
    val title: String,
    val description: String = ""
)
