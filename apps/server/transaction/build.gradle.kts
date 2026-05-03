plugins {
    id("artifact")
}

dependencies {
    implementation(project(":account-api"))
    implementation(project(":common"))
    implementation(project(":contract"))
    implementation(project(":currency-api"))
    implementation(project(":partner-api"))
    implementation(project(":pocket-api"))
    implementation(project(":transaction-api"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    runtimeOnly("org.postgresql:r2dbc-postgresql")
}
