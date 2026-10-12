package com.goldenboot.goldenbootfinancial.payroll.feature.payroll.data.dto

import com.goldenboot.goldenbootfinancial.payroll.feature.payroll.domain.model.PayrollItem
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Payroll.
 *
 * Maps network responses to domain models.
 */
@Serializable
data class PayrollDto(
    val id: String,
    val title: String,
    val description: String? = null
) {
    fun toDomain(): PayrollItem {
        return PayrollItem(
            id = id,
            title = title,
            description = description.orEmpty()
        )
    }
}
