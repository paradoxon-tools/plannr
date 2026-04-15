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
    ":contract-api",
    ":currency-api",
    ":partner-api",
    ":pocket-api",
    ":transaction-api",
)
