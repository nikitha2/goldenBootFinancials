package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.model

/**
 * Domain model for Payroll.
 */
data class PayrollItem(
    val id: String,
    val title: String,
    val description: String = ""
)
