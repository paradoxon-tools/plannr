import java.util.concurrent.TimeUnit

plugins {
    id("artifact")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":partner-api"))
    implementation(project(":partner-shared"))
    implementation(project(":transaction-projection-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.21.2")
    implementation("com.twelvemonkeys.imageio:imageio-bmp:3.12.0")

    runtimeOnly("org.postgresql:r2dbc-postgresql")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
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
