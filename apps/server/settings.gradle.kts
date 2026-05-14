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
    ":health",
    ":partner",
    ":partner-api",
    ":partner-shared",
    ":pocket",
    ":pocket-api",
    ":pocket-shared",
    ":transaction-materialization",
    ":transaction-materialization-shared",
    ":transaction-projection",
    ":transaction-projection-shared",
    ":transaction-template",
    ":transaction-template-api",
    ":transaction-template-shared",
)
