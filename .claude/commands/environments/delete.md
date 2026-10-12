---
description: Remove an environment (product flavor) from the project
---

## Your task

Remove an environment from this KMP project. Ask the user which environment to remove.

### Step 1: Identify the flavor

Open `composeApp/build.gradle.kts` and find the `productFlavors { }` block. List the existing flavors for the user to choose from.

**Important:** The flavor name may differ from the environment name. `TEST` environment uses the flavor name `qa` (Android Gradle Plugin reserves the "test" prefix).

### Step 2: Remove the product flavor

Delete the entire `create("<flavorName>") { ... }` block for the selected environment from `composeApp/build.gradle.kts`.

**Warning:** Do NOT remove the last remaining product flavor. At least one environment must exist. Warn the user if they try to delete the only remaining flavor.

### Step 3: Clean up environment-specific source sets (if any)

Check if there are environment-specific source set directories:
- `composeApp/src/<flavorName>Main/`
- `composeApp/src/<flavorName>Debug/`
- `composeApp/src/<flavorName>Release/`

If they exist, ask the user whether to delete them.

### Step 4: Update EnvironmentSetup.md

Open `docs/EnvironmentSetup.md` and remove:
1. The environment from the environment list
2. The build variants (`<flavorName>Debug` / `<flavorName>Release`)
3. The build commands section for this environment

### Step 5: Verify

Run `./gradlew :composeApp:tasks` and confirm the removed build variants no longer appear.

### Step 6: Report

Show the user what was removed and the remaining available environments.
