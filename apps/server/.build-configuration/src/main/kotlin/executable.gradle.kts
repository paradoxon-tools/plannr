import extensions.DatabaseTechnology.MONGO
import extensions.DatabaseTechnology.POSTGRES
import extensions.config
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.language.jvm.tasks.ProcessResources
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("internal.common")
    id("org.springframework.boot")
}

configurations.configureEach {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

springBoot {
    buildInfo()
}

val monorepoRoot = rootProject.projectDir.parentFile?.parentFile ?: rootProject.projectDir

tasks.withType<BootRun> {
    workingDir(monorepoRoot)
}

val springCloudEnabled = config.features.springCloudEnabled.get()
val springConfigImport = if (springCloudEnabled) {
    "config:${System.lineSeparator()}    import: \"optional:configserver:\""
} else {
    "config: {}"
}

tasks.withType<ProcessResources> {
    filesMatching("application*.yml") {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "springConfigImport" to springConfigImport
            )
        )
    }
}

dependencies {

    // The "app-*" projects import every other sub-project except for other "app-*" and "*-test" projects
    rootProject.subprojects.forEach {
        if (it.path != project.path && !it.name.startsWith("app-") && !it.name.endsWith("-test")) {
            implementation(it)
        }
    }

    config.features.starters.get().forEach {
        implementation(it)
    }

    implementation(config.features.dbTechnology.map {
        when (it) {
            MONGO -> "org.springframework.boot:spring-boot-starter-data-mongodb-reactive"
            POSTGRES -> "org.springframework.boot:spring-boot-starter-data-r2dbc"
        }
    })

    runtimeOnly(config.features.dbTechnology.map {
        when (it) {
            MONGO -> null
            POSTGRES -> "org.postgresql:r2dbc-postgresql"
        }
    })

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    if (springCloudEnabled) {
        implementation("org.springframework.cloud:spring-cloud-starter-config")
    }
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
