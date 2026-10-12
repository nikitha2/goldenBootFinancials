# Ktor Server

## Overview

Ktor backend server module for the application. Runs on Netty with JSON serialization support.

## Package

`com.goldenboot.goldenbootfinancial`

## Running

```bash
./gradlew :server:run
```

The server starts on `http://0.0.0.0:8080`.

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Server status message |
| GET | `/health` | Health check endpoint |

## Testing

```bash
./gradlew :server:test
```

Uses Ktor's `testApplication` for in-process server testing without starting a real server.

## Dependencies

- **ktor-server-core** — Core server framework
- **ktor-server-netty** — Netty engine
- **ktor-server-content-negotiation** — Content type handling
- **ktor-serialization-kotlinx-json** — JSON serialization
- **logback** — Logging
- **core** — Shared KMP core module

## Environments

| Environment | Base URL |
|-------------|----------|
| MOCK | `http://10.0.2.2:8080/` |
| TEST | `https://test.bff.goldenboot.com` |
| PROD | `https://bff.goldenboot.com` |

## Adding New Routes

Add routes in `Routing.kt`:

```kotlin
fun Application.configureRouting() {
    routing {
        get("/api/items") {
            call.respond(listOf("item1", "item2"))
        }
    }
}
```

## Architecture

```
server/
├── build.gradle.kts
└── src/
    ├── main/kotlin/com/goldenboot/goldenbootfinancial/
    │   ├── Application.kt      — Entry point, server config
    │   ├── Routing.kt           — Route definitions
    │   └── Serialization.kt     — JSON serialization setup
    └── test/kotlin/com/goldenboot/goldenbootfinancial/
        └── ApplicationTest.kt   — Route tests
```

## Configuration (environment variables)

| Variable | Default | Purpose |
|---|---|---|
| `BFF_MODE` | `mock` | `mock` serves sample data; `proxy` calls the real backend. |
| `UPSTREAM_BASE_URL` | `https://api.goldenboot.com` | The FastAPI backend (proxy mode). |
| `WEB_APP_BASE_URL` | `https://app.goldenbootfinancial.com` | Web app whose `/api/recommendation` holds the Game Plan and wellness rules. |
| `BFF_ALLOW_ANONYMOUS` | `false` | Forward calls with no token. Only for a backend running its demo-auth switch. |
| `CORS_ALLOWED_ORIGINS` | none | Comma-separated origins allowed from a browser. Mock mode allows any. |

Docker: build the jar first (`./gradlew :server:buildFatJar`), then `docker compose up -d --build`.
