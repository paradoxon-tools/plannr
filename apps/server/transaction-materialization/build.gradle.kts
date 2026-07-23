plugins {
    id("artifact")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":pocket-api"))
    implementation(project(":pocket-shared"))
    implementation(project(":transaction-materialization-shared"))
    implementation(project(":transaction-projection-shared"))
    implementation(project(":transaction-template-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework:spring-web")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
