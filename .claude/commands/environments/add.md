---
description: Add a new environment (product flavor) to the project
---

## Your task

Add a new environment to this KMP project. Ask the user for:
1. **Environment name** (e.g., STAGING, QA, DEV)
2. **Base URL** for the environment

### Step 1: Add the product flavor

Open `composeApp/build.gradle.kts` and find the `productFlavors { }` block inside `android { }`.

Add a new flavor using this pattern:

```kotlin
create("<flavorName>") {
    dimension = "environment"
    applicationIdSuffix = ".<envNameLower>"
    versionNameSuffix = "-<envNameLower>"
    buildConfigField("String", "BASE_URL", "\"<baseUrl>\"")
    buildConfigField("String", "ENVIRONMENT", "\"<ENV_NAME>\"")
    resValue("string", "app_name", "<ProjectName> <ENV_NAME>")
}
```

**Important naming rules:**
- `<flavorName>`: Use lowercase environment name, EXCEPT for `TEST` which must use `qa` (Android Gradle Plugin reserves the "test" prefix for unit-test source sets)
- `<envNameLower>`: Always the lowercase environment name (e.g., `staging`)
- `<ENV_NAME>`: The original environment name as provided by the user
- For PROD environment, `resValue` app_name should be just the project name without suffix

### Step 2: Update EnvironmentSetup.md

Open `docs/EnvironmentSetup.md` and add the new environment to:
1. The environment list at the top
2. The build variants section (add `<flavorName>Debug` and `<flavorName>Release`)
3. The build commands section with platform-specific commands:

```bash
# Android
./gradlew :composeApp:assemble<FlavorNameCapitalized>Debug
./gradlew :composeApp:assemble<FlavorNameCapitalized>Release
```

### Step 3: Verify

Run `./gradlew :composeApp:tasks` and confirm the new build variants appear (e.g., `assemble<FlavorName>Debug`).

### Step 4: Report

Show the user what was added and the available build commands for the new environment.
