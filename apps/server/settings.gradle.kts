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
    ":transaction",
    ":transaction-api",
    ":transaction-shared",
)
