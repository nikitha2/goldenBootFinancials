package com.goldenboot.goldenbootfinancial.profile.feature.profile.data.dto

import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.model.ProfileItem
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Profile.
 *
 * Maps network responses to domain models.
 */
@Serializable
data class ProfileDto(
    val id: String,
    val title: String,
    val description: String? = null
) {
    fun toDomain(): ProfileItem {
        return ProfileItem(
            id = id,
            title = title,
            description = description.orEmpty()
        )
    }
}
