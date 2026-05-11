plugins {
    id("artifact")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":transaction-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}
