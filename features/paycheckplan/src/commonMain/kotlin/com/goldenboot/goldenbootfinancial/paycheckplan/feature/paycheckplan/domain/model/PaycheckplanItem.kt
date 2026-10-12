package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.model

/**
 * Domain model for PaycheckPlan.
 */
data class PaycheckplanItem(
    val id: String,
    val title: String,
    val description: String = ""
)
