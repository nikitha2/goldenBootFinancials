package com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.data.dto

import com.goldenboot.goldenbootfinancial.cashflow.feature.cashflow.domain.model.CashflowItem
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for CashFlow.
 *
 * Maps network responses to domain models.
 */
@Serializable
data class CashflowDto(
    val id: String,
    val title: String,
    val description: String? = null
) {
    fun toDomain(): CashflowItem {
        return CashflowItem(
            id = id,
            title = title,
            description = description.orEmpty()
        )
    }
}
