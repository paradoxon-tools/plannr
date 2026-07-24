plugins {
    id("artifact")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":contract-api"))
    implementation(project(":contract-shared"))
    implementation(project(":financial-profile-api"))
    implementation(project(":financial-profile-shared"))
    implementation(project(":transaction-materialization-shared"))
    implementation(project(":transaction-projection-shared"))
    implementation(project(":transaction-template-api"))
    implementation(project(":transaction-template-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    runtimeOnly("org.postgresql:r2dbc-postgresql")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
