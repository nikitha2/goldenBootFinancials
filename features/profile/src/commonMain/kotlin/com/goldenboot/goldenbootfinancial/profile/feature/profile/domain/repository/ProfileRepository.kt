package com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.repository

import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.model.ProfileItem

/**
 * Repository contract for Profile data access.
 */
interface ProfileRepository {
    suspend fun getAll(): List<ProfileItem>
    suspend fun getById(id: String): ProfileItem?
}
