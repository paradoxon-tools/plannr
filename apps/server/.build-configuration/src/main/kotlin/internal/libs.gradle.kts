package internal

import extensions.ApplicationType.WEB
import extensions.config

plugins {
    id("java")
}

dependencies {

    implementation(config.features.applicationType.map {
        when (it) {
            WEB -> "org.springframework.boot:spring-boot-starter-web"
            else -> null
        }
    })

    config.features.commonLibs.get().forEach {
        implementation(it)
    }

    config.features.annotationProcessors.get().forEach {
        compileOnly(it)
        annotationProcessor(it)
        testCompileOnly(it)
        testAnnotationProcessor(it)
    }
}
