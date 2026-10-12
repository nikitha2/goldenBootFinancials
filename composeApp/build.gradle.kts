import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.navigation.compose)

                // Koin for dependency injection
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                // Project modules
                implementation(projects.core)
                implementation(projects.features.dashboard)
                implementation(projects.features.cashflow)
                implementation(projects.features.payroll)
                implementation(projects.features.paycheckplan)
                implementation(projects.features.investments)
                implementation(projects.features.profile)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.koin.android)
                implementation(libs.clerk.android.api)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(npm("@js-joda/timezone", "2.18.2"))
            }
        }
    }
}

android {
    namespace = "com.goldenboot.goldenbootfinancial"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.goldenboot.goldenbootfinancial"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        // Clerk publishable key (pk_test_... / pk_live_...). It is public, but kept out of the repo:
        // set `clerk.publishableKey` in local.properties or ~/.gradle/gradle.properties. Without it the
        // app builds and runs, and sign-in reports "not set up in this build".
        val localProperties = Properties().apply {
            rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
        }
        val clerkKey = (findProperty("clerk.publishableKey") as String?)
            ?: localProperties.getProperty("clerk.publishableKey")
            ?: ""
        buildConfigField("String", "CLERK_PUBLISHABLE_KEY", "\"$clerkKey\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("qa") {
            dimension = "environment"
            applicationIdSuffix = ".test"
            versionNameSuffix = "-test"
            buildConfigField("String", "ENVIRONMENT", "\"TEST\"")
            resValue("string", "app_name", "GoldenbootFinancial TEST")
        }

        create("prod") {
            dimension = "environment"
            applicationIdSuffix = ".prod"
            versionNameSuffix = "-prod"
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
            resValue("string", "app_name", "GoldenbootFinancial")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.goldenboot.goldenbootfinancial.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.goldenboot.goldenbootfinancial"
            packageVersion = "1.0.0"
        }
    }
}
