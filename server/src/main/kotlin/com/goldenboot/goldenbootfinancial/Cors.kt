package com.goldenboot.goldenbootfinancial

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Lets the web app (a different origin) call the BFF from the browser.
 *
 * `CORS_ALLOWED_ORIGINS` is a comma-separated list such as `https://app.goldenboot.com`.
 * When unset, any origin is allowed only in mock mode (local development).
 */
fun Application.configureCors(config: BffConfig = BffConfig()) {
    val origins = System.getenv("CORS_ALLOWED_ORIGINS")
        ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("X-Request-Id")
        when {
            origins.isNotEmpty() -> origins.forEach { origin ->
                val scheme = origin.substringBefore("://")
                allowHost(origin.substringAfter("://"), schemes = listOf(scheme))
            }
            !config.proxy -> anyHost()
        }
    }
}
