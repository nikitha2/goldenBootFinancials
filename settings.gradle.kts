rootProject.name = "GoldenbootFinancial"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":composeApp")
include(":core")
include(":features:dashboard")
include(":features:cashflow")
include(":features:payroll")
include(":features:paycheckplan")
include(":features:investments")
include(":features:profile")
include(":server")
