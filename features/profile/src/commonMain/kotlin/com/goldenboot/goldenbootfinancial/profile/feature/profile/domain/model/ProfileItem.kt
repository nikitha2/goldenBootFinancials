package com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.model

/**
 * Domain model for Profile.
 */
data class ProfileItem(
    val id: String,
    val title: String,
    val description: String = ""
)
