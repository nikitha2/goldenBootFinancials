package com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.service

import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.model.ProfileItem
import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.repository.ProfileRepository

/**
 * Service layer for Profile business logic.
 */
class ProfileService(
    private val repository: ProfileRepository
) {
    suspend fun getAll(): List<ProfileItem> {
        return repository.getAll()
    }

    suspend fun getById(id: String): ProfileItem? {
        return repository.getById(id)
    }
}
