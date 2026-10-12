# GoldenbootFinancial

A Kotlin Multiplatform application built with Compose Multiplatform.

## Features

- ✅ Kotlin Multiplatform (Android, Ios, Desktop, Web, Server)
- ✅ Compose Multiplatform for UI
- ✅ Koin for Dependency Injection
- ✅ Multi-environment support (configured)
- ✅ Modular architecture

## Modules

- **core**: Core Module
- **dashboard**: Dashboard
- **cashflow**: CashFlow
- **payroll**: Payroll
- **paycheckplan**: PaycheckPlan
- **investments**: Investments
- **profile**: Profile

## Getting Started

### Prerequisites

- JDK 17 or higher
- Android Studio Ladybug or higher
- Xcode (for iOS development)

### Build and Run

#### Android
```bash
./gradlew :composeApp:assembleDebug
```

#### iOS
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

#### Desktop
```bash
./gradlew :composeApp:run --no-configuration-cache
```
> **Note:** The `--no-configuration-cache` flag avoids Gradle configuration cache issues that can cause `metadata.bin` errors. If you encounter a `Could not read workspace metadata` error, stop the Gradle daemon and clear caches:
> ```bash
> ./gradlew --stop && rm -rf ~/.gradle/caches/$(./gradlew --version | grep "^Gradle" | awk '{print $2}') .gradle
> ```

#### Web
```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

#### Server
```bash
./gradlew :server:run
```


## Troubleshooting

### Emulator: Not enough space to create userdata partition

If you see an error like:
```
FATAL | Not enough space to create userdata partition. Available: XXXX MB, need YYYY MB.
```

Follow these steps to free up disk space:

1. **Delete emulator snapshots**
   ```bash
   rm -rf ~/.android/avd/<YourAVD>.avd/snapshots/
   ```

2. **Clear Android emulator cache**
   ```bash
   rm -rf ~/.android/cache/
   ```

3. **Clear Gradle caches**
   ```bash
   rm -rf ~/.gradle/caches/
   ```

4. **Clear Xcode derived data (macOS)**
   ```bash
   rm -rf ~/Library/Developer/Xcode/DerivedData/
   ```

5. **Reduce the emulator's data partition size**

   Edit `~/.android/avd/<YourAVD>.avd/config.ini` and lower the `disk.dataPartition.size` value (in bytes):
   ```
   # 4 GB (default is often 6 GB)
   disk.dataPartition.size = 4294967296
   ```

6. **Wipe emulator data and restart**
   ```bash
   $ANDROID_HOME/emulator/emulator -avd <YourAVD> -wipe-data
   ```

> **Note:** Clearing Gradle caches and Xcode derived data is safe — they will be rebuilt automatically on the next build.

## Environment Configuration

See [core/doc/EnvironmentSetup.md](core/doc/EnvironmentSetup.md) for detailed information about environment setup.

## Project Structure

```
GoldenbootFinancial/
├── composeApp/          # Main application module
│   └── src/
│       ├── commonMain/  # Shared code across all platforms
│       ├── androidMain/ # Android-specific code
│       ├── iosMain/     # iOS-specific code
│       ├── desktopMain/ # Desktop (JVM) code
│       └── wasmJsMain/  # Web-specific code
├── core/               # Core Module module
├── features/         # Feature modules
│   ├── dashboard/           # Dashboard module
│   ├── cashflow/           # CashFlow module
│   ├── payroll/           # Payroll module
│   ├── paycheckplan/           # PaycheckPlan module
│   ├── investments/           # Investments module
│   ├── profile/           # Profile module
└── buildSrc/            # Build convention plugins
```

## Documentation

- [Environment Setup](core/doc/EnvironmentSetup.md)

---

Generated with KMP Scaffolder MCP
