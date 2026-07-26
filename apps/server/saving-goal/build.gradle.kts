plugins {
    id("artifact")
}

dependencies {
    implementation(project(":account-api"))
    implementation(project(":account-shared"))
    implementation(project(":common"))
    implementation(project(":financial-profile-api"))
    implementation(project(":financial-profile-shared"))
    implementation(project(":pocket-api"))
    implementation(project(":pocket-shared"))
    implementation(project(":saving-goal-api"))
    implementation(project(":saving-goal-shared"))
    implementation(project(":transaction-projection-api"))
    implementation(project(":transaction-projection-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springframework:spring-web")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
