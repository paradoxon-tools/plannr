package internal

import extensions.config

plugins {
    id("java")
}

dependencies {

    if (config.features.springCloudEnabled.get()) {
        implementation(config.versions.springCloud.map {
            platform("org.springframework.cloud:spring-cloud-dependencies:$it")
        })
    }

    implementation(config.versions.springBoot.map {
        platform("org.springframework.boot:spring-boot-dependencies:$it")
    })

    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    constraints {
        implementation(config.versions.springOpenApi.map {
            "org.springdoc:springdoc-openapi-starter-webflux-ui:$it"
        })
    }
}
