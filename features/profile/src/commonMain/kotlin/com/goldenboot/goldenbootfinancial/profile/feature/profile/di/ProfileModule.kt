package com.goldenboot.goldenbootfinancial.profile.feature.profile.di

import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.goldenboot.goldenbootfinancial.profile.feature.profile.data.api.ProfileApi
import com.goldenboot.goldenbootfinancial.profile.feature.profile.data.repository.ProfileRepositoryImpl
import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.repository.ProfileRepository
import com.goldenboot.goldenbootfinancial.profile.feature.profile.domain.service.ProfileService
import com.goldenboot.goldenbootfinancial.profile.feature.profile.presentation.ProfileViewModel

/**
 * Koin module for Profile feature.
 *
 * Provides dependencies specific to the Profile module.
 */
val profileModule = module {
    // Data layer
    single { ProfileApi(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }

    // Domain layer
    single { ProfileService(get()) }

    // Presentation layer
    viewModel { ProfileViewModel(get()) }
}
