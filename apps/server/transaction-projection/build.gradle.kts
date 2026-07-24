plugins {
    id("artifact")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":transaction-projection-api"))
    implementation(project(":transaction-projection-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework:spring-web")

    runtimeOnly("org.postgresql:r2dbc-postgresql")
}
