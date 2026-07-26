dependencyResolutionManagement {
    versionCatalogs.create("libs").from(files("versions.toml"))
}

pluginManagement {
    includeBuild(".build-configuration")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "server"
include(
    ":app",
    ":account",
    ":account-api",
    ":account-shared",
    ":common",
    ":contract",
    ":contract-api",
    ":contract-shared",
    ":financial-profile",
    ":financial-profile-api",
    ":financial-profile-shared",
    ":health",
    ":partner",
    ":partner-api",
    ":partner-shared",
    ":pocket",
    ":pocket-api",
    ":pocket-shared",
    ":saving-goal",
    ":saving-goal-api",
    ":saving-goal-shared",
    ":transaction-materialization",
    ":transaction-materialization-api",
    ":transaction-materialization-shared",
    ":transaction-projection",
    ":transaction-projection-api",
    ":transaction-projection-shared",
    ":transaction-template",
    ":transaction-template-api",
    ":transaction-template-shared",
)
