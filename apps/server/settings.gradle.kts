plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
