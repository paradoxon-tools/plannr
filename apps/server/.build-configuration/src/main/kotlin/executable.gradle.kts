import extensions.DatabaseTechnology.MONGO
import extensions.DatabaseTechnology.POSTGRES
import extensions.config

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
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
