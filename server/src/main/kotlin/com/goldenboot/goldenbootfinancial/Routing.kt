package com.goldenboot.goldenbootfinancial

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/** One BFF screen served from mock data (until its real upstream calls are wired). */
private class Screen(val path: String, val mock: List<Item>)

private val screens = listOf(
    Screen("/cashflow", MockData.cashflow),
    Screen("/payroll", MockData.payroll),
    Screen("/paycheckplan", MockData.paycheckplan),
    Screen("/investments", MockData.investments),
    Screen("/profile-items", MockData.profile)
)

fun Application.configureRouting(
    config: BffConfig = BffConfig(),
    upstream: UpstreamClient = UpstreamClient(config.upstreamBaseUrl),
    store: MockStore = MockStore(),
    recommender: RecommendationClient = RecommendationClient(config.webAppBaseUrl)
) {
    routing {
        get("/") { call.respondText("Server is running") }
        get("/health") { call.respondText("OK") }

        // Foundation: profile + legal status (real upstream calls in proxy mode).
        foundationRoutes(config, upstream, store)

        get("/dashboard") {
            if (!config.proxy) return@get call.respond(MockData.dashboard)

            // Real backend: profile, agreements, accounts, history, plus the web app's rules engine.
            // `?date=YYYY-MM-DD` is the member's local day (history is keyed by it).
            val auth = call.request.headers["Authorization"]
            if (auth == null && !config.allowAnonymous) {
                return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Sign in required."))
            }
            try {
                call.respond(
                    buildDashboard(
                        upstream, recommender, auth, call.request.headers["X-Request-Id"],
                        resolveToday(call.request.queryParameters["date"])
                    )
                )
            } catch (error: Throwable) {
                call.respondUpstreamError(error)
            }
        }

        // Screens not wired to the real backend yet: mock data in mock mode,
        // an honest 501 in proxy mode (the upstream has no matching path to call).
        screens.forEach { screen ->
            get(screen.path) {
                if (config.proxy) return@get call.respondNotWired(screen.path.trim('/'))
                call.respond(screen.mock)
            }
            get("${screen.path}/{id}") {
                if (config.proxy) return@get call.respondNotWired(screen.path.trim('/'))
                val id = call.parameters["id"]
                val item = screen.mock.firstOrNull { it.id == id }
                if (item != null) call.respond(item) else call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private suspend fun ApplicationCall.respondNotWired(name: String) =
    respond(HttpStatusCode.NotImplemented, mapOf("error" to "'$name' is not wired to the backend yet."))

/** Relays upstream 4xx/5xx statuses (401/403/404 included); anything else becomes 502. */
internal suspend fun ApplicationCall.respondUpstreamError(error: Throwable) {
    val status = (error as? UpstreamException)?.status ?: HttpStatusCode.BadGateway
    respond(status, mapOf("error" to (error.message ?: "Upstream error")))
}
