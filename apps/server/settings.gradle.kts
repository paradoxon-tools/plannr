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
    ":common",
    ":contract",
    ":contract-api",
    ":currency",
    ":currency-api",
    ":health",
    ":partner",
    ":partner-api",
    ":pocket",
    ":pocket-api",
    ":transaction",
    ":transaction-api",
)
