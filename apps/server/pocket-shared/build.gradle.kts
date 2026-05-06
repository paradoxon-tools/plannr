plugins {
    id("artifact-shared")
}

dependencies {
    implementation(project(":contract-api"))
    implementation(project(":pocket-api"))
}
