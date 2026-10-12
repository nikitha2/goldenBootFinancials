package com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.data.dto

import com.goldenboot.goldenbootfinancial.paycheckplan.feature.paycheckplan.domain.model.PaycheckplanItem
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for PaycheckPlan.
 *
 * Maps network responses to domain models.
 */
@Serializable
data class PaycheckplanDto(
    val id: String,
    val title: String,
    val description: String? = null
) {
    fun toDomain(): PaycheckplanItem {
        return PaycheckplanItem(
            id = id,
            title = title,
            description = description.orEmpty()
        )
    }
}
