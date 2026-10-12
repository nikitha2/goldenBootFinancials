package com.goldenboot.goldenbootfinancial.investments.feature.investments.data.dto

import com.goldenboot.goldenbootfinancial.investments.feature.investments.domain.model.InvestmentsItem
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Investments.
 *
 * Maps network responses to domain models.
 */
@Serializable
data class InvestmentsDto(
    val id: String,
    val title: String,
    val description: String? = null
) {
    fun toDomain(): InvestmentsItem {
        return InvestmentsItem(
            id = id,
            title = title,
            description = description.orEmpty()
        )
    }
}
