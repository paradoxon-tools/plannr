import java.util.concurrent.TimeUnit
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("executable")
}

description = "Coroutine-first Spring Boot version of plannr-server"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("tools.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.4"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
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

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xannotation-default-target=param-property",
            "-Xconsistent-data-class-copy-visibility"
        )
    }
}
