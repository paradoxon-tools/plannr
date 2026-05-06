import java.util.concurrent.TimeUnit

plugins {
    id("test-artifact")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":contract-api"))
    implementation(project(":contract-shared"))
    implementation(project(":partner-api"))
    implementation(project(":partner-shared"))
    implementation(project(":pocket-api"))
    implementation(project(":pocket-shared"))
    implementation(project(":transaction-api"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework:spring-web")

    runtimeOnly("org.postgresql:r2dbc-postgresql")

    testImplementation(project(":account"))
    testImplementation(project(":account-api"))
    testImplementation(project(":partner"))
    testImplementation(project(":partner-api"))
    testImplementation(project(":partner-shared"))
    testImplementation(project(":pocket"))
    testImplementation(project(":pocket-api"))
    testImplementation(project(":pocket-shared"))
    testImplementation(project(":transaction-shared"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.flywaydb:flyway-core")
    testImplementation("org.flywaydb:flyway-database-postgresql")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.4"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.postgresql:postgresql")
}

val dockerAvailable = runCatching {
    val process = ProcessBuilder("docker", "info")
        .redirectErrorStream(true)
        .start()
    process.inputStream.bufferedReader().use { it.readText() }
    process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0
}.getOrDefault(false)

tasks.withType<Test> {
    useJUnitPlatform {
        if (!dockerAvailable) {
            excludeTags("integration")
        }
    }
    doFirst {
        if (!dockerAvailable) {
            logger.lifecycle("Docker is not available; skipping integration tests tagged 'integration'.")
        }
    }
}

tasks.processTestResources {
    from("../app/src/main/resources/db/migration") {
        into("db/migration")
    }
}
