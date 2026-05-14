plugins {
    id("artifact")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":transaction-projection-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
}
