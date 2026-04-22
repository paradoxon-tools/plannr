import java.util.concurrent.TimeUnit

plugins {
    id("artifact")
}

dependencies {
    implementation(project(":account-api"))
    implementation(project(":common"))
    implementation(project(":currency-api"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework:spring-web")

    runtimeOnly("org.postgresql:r2dbc-postgresql")

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
