package com.goldenboot.goldenbootfinancial.core.foundation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** The member's saved profile. [profile] is the free-form JSON written by the profile builder. */
@Serializable
data class ProfileSnapshot(
    val profile: JsonObject,
    val version: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ProfileStatus(val saved: Boolean, val version: Int, val updatedAt: String? = null)

@Serializable
data class ProfileSaveResult(val status: String, val version: Int, val updatedAt: String? = null)

/** Agreement and questionnaire gates. */
@Serializable
data class LegalStatus(
    val allDocumentsAccepted: Boolean,
    val intakeComplete: Boolean,
    val questionnaireComplete: Boolean,
    val betaOnboardingComplete: Boolean
)
