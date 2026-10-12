package com.goldenboot.goldenbootfinancial.core.network

import com.goldenboot.goldenbootfinancial.core.auth.AuthTokenProvider
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode

class BearerAuthConfig {
    var tokenProvider: AuthTokenProvider? = null
    var onUnauthorized: () -> Unit = {}
}

/**
 * Attaches `Authorization: Bearer <token>` to every request when a token exists, and reports 401
 * responses so the session can be cleared. Requests without a token go out unauthenticated (the BFF
 * answers 401, or serves mock data in local development).
 */
val BearerAuth = createClientPlugin("BearerAuth", ::BearerAuthConfig) {
    val provider = pluginConfig.tokenProvider
    val onUnauthorized = pluginConfig.onUnauthorized

    onRequest { request, _ ->
        provider?.accessToken()?.let { request.headers.append(HttpHeaders.Authorization, "Bearer $it") }
    }
    onResponse { response ->
        if (response.status == HttpStatusCode.Unauthorized) onUnauthorized()
    }
}
