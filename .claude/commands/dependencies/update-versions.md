---
description: Fetch latest stable dependency versions and update gradle/libs.versions.toml
---

## Your task

Update the dependency versions in `gradle/libs.versions.toml` to the latest stable releases.

### Step 1: Read current versions

Read `gradle/libs.versions.toml` and note every version variable under `[versions]`.

### Step 2: Fetch latest stable versions

For each dependency, fetch the latest **stable** version (no alpha, beta, RC, dev, SNAPSHOT suffixes):

| Dependency | Maven Coordinates | Repository |
|---|---|---|
| kotlin | org.jetbrains.kotlin:kotlin-stdlib | Maven Central |
| compose | org.jetbrains.compose:compose-gradle-plugin | Maven Central |
| agp | com.android.tools.build:gradle | Maven Central |
| androidxActivityCompose | androidx.activity:activity-compose | Maven Central |
| koin | io.insert-koin:koin-core | Maven Central |
| lifecycle | org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose | JetBrains Maven (maven.pkg.jetbrains.space/public/p/compose/dev) |
| navigation | org.jetbrains.androidx.navigation:navigation-compose | JetBrains Maven (maven.pkg.jetbrains.space/public/p/compose/dev) |
| ktor | io.ktor:ktor-client-core | Maven Central |
| kotlinxSerialization | org.jetbrains.kotlinx:kotlinx-serialization-json | Maven Central |
| room | androidx.room:room-runtime | Maven Central |
| sqlite | androidx.sqlite:sqlite-bundled | Maven Central |
| ksp | com.google.devtools.ksp:symbol-processing-api | Maven Central (match Kotlin version prefix) |
| gradle | Gradle wrapper | https://services.gradle.org/versions/current |

**How to query Maven Central:**
```
https://search.maven.org/solrsearch/select?q=g:{groupId}+AND+a:{artifactId}&core=gav&rows=20&wt=json
```
Filter `response.docs[].v` for stable versions only.

**How to query JetBrains Maven:**
```
https://maven.pkg.jetbrains.space/public/p/compose/dev/{groupPath}/{artifactId}/maven-metadata.xml
```
Parse `<version>` tags from the XML and filter for stable versions.

### Step 3: Validate compatibility

Apply these rules before updating. Reference the official compatibility guide:
https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html

1. **AGP ↔ Kotlin**: AGP version must be within the compatible range for the Kotlin version. Check the official matrix.
2. **Gradle ↔ Kotlin**: Gradle version must be within the compatible range for the Kotlin version.
3. **Navigation/Lifecycle ↔ Compose MP**: Navigation and lifecycle versions must not exceed the max compatible version for the Compose Multiplatform version.
4. **Ktor**: If Ktor >= 3.2.0, cap at 3.1.3 (D8 dexing bug on Android).
5. **KSP ↔ Kotlin**: KSP version must have a Kotlin prefix matching the resolved Kotlin version (e.g., Kotlin 2.3.0 → KSP 2.3.0-x.x.x).

If any version fails validation, adjust it to the max compatible version and note the adjustment.

### Step 4: Update libs.versions.toml

Update the `[versions]` section in `gradle/libs.versions.toml` with the validated versions. Do NOT change the `[libraries]` or `[plugins]` sections — only version numbers.

### Step 5: Update Gradle wrapper (if needed)

If the Gradle version changed, run:
```bash
./gradlew wrapper --gradle-version {newGradleVersion}
```

### Step 6: Report

Show a summary table of what changed:
| Dependency | Old Version | New Version | Notes |
|---|---|---|---|
| ... | ... | ... | (any compatibility adjustments) |
