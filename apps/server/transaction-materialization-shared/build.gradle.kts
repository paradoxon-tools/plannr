plugins {
    id("artifact-shared")
}

dependencies {
    implementation(project(":transaction-materialization-api"))
    implementation(project(":transaction-template-shared"))
}
