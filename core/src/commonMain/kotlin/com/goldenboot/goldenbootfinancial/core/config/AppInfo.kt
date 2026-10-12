package com.goldenboot.goldenbootfinancial.core.config

/**
 * Build identity shown in the UI (drawer footer).
 *
 * @param version Marketing version without any flavor suffix, e.g. "1.0.0".
 * @param environment "TEST" or "PROD".
 */
data class AppInfo(
    val version: String = DEFAULT_VERSION,
    val environment: String = "TEST"
) {
    /** "v1.0.0 · TEST" on TEST builds, just "v1.0.0" on PROD. */
    val displayText: String
        get() = if (environment.equals("PROD", ignoreCase = true)) "v$version"
        else "v$version · ${environment.uppercase()}"

    companion object {
        const val DEFAULT_VERSION = "1.0.0"
    }
}
