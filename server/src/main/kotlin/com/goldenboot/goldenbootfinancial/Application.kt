package com.goldenboot.goldenbootfinancial

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging

fun main() {
    embeddedServer(
        Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

const val SERVER_PORT = 8080

fun Application.module() {
    install(CallLogging) // one INFO line per request: method, path, status
    configureSerialization()
    configureCors()
    configureRouting()
}
