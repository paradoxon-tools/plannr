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
    ":account-api",
    ":common",
    ":contract-api",
    ":currency",
    ":currency-api",
    ":partner",
    ":partner-api",
    ":pocket-api",
    ":transaction-api",
)
