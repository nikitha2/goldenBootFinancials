package com.goldenboot.goldenbootfinancial.core.config

/**
 * Single source of truth for the BFF base URLs, shared by every app target.
 *
 * TEST points at the BFF on the developer machine (docker-compose.yml publishes [LOCAL_PORT]).
 * Desktop, iOS simulator and web reach it as localhost directly. The Android emulator reaches
 * the host's localhost through adb:
 *
 *     adb reverse tcp:8090 tcp:8090
 *
 * When the BFF is hosted, change [TEST_BASE_URL] here and every platform follows.
 */
object ApiConfig {
    const val LOCAL_PORT = 8090

    const val TEST_BASE_URL = "http://localhost:$LOCAL_PORT/"

    const val PROD_BASE_URL = "https://bff.goldenboot.com"

    /** Maps an environment name ("TEST" / "PROD") to its URL. */
    fun baseUrlFor(environment: String): String = when (environment.uppercase()) {
        "PROD" -> PROD_BASE_URL
        else -> TEST_BASE_URL
    }
}
