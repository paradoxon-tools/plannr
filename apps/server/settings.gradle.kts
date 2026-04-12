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
    ":recurring-transaction-api",
    ":transaction-api",
    ":account-query-api",
    ":contract-query-api",
    ":pocket-query-api",
    ":transaction-query-api",
)
