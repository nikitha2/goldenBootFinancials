package com.goldenboot.goldenbootfinancial.profile.feature.profile.data.repository

import com.goldenboot.goldenbootfinancial.core.network.NetworkResult
import com.goldenboot.goldenbootfinancial.core.network.networkFetch
import com.goldenboot.goldenbootfinancial.profile.feature.profile.data.api.ProfileApi
import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.model.ProfileItem
import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.repository.ProfileRepository

/**
 * Repository implementation for Profile.
 *
 * Fetches data from the network API with call deduplication.
 * Retries are handled by Ktor's HttpRequestRetry plugin (2 retries with exponential backoff).
 */
class ProfileRepositoryImpl(
    private val api: ProfileApi
) : ProfileRepository {
    override suspend fun getAll(): List<ProfileItem> {
        return when (val result = networkFetch { api.fetchAll() }) {
            is NetworkResult.Success -> result.data.map { it.toDomain() }
            is NetworkResult.Error -> throw result.exception
        }
    }

    override suspend fun getById(id: String): ProfileItem? {
        return when (val result = networkFetch { api.fetchById(id) }) {
            is NetworkResult.Success -> result.data?.toDomain()
            is NetworkResult.Error -> throw result.exception
        }
    }
}
