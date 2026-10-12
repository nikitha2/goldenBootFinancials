package com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.model

/**
 * Domain model for Investments.
 */
data class InvestmentsItem(
    val id: String,
    val title: String,
    val description: String = ""
)
