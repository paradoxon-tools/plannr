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

rootProject.name = "plannr-server"
include(
    ":app",
    ":account",
    ":account-api",
    ":common",
    ":contract-api",
    ":currency",
    ":currency-api",
    ":partner",
    ":partner-api",
    ":pocket",
    ":pocket-api",
    ":transaction-api",
)
