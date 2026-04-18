@file:Suppress("UnstableApiUsage")

import extensions.ApplicationType
import extensions.DatabaseTechnology

plugins {
    id("build-process")
}

subprojects {
    repositories {
        mavenCentral()
    }
}

buildProcess {

    features {
        applicationType = ApplicationType.WEB
        dbTechnology = DatabaseTechnology.POSTGRES
        springCloudEnabled = true

        starters.addAll(
        )

        commonLibs.addAll(
        )
        commonTestLibs.addAll(
        )
    }

    docker {
        appName = rootProject.name
        baseDockerImageVersion = libs.versions.docker.baseImage
    }

    versions {
        javaLanguage = libs.versions.java

        springBoot = libs.versions.spring.boot
        springCloud = libs.versions.spring.cloud
        springOpenApi = libs.versions.spring.openapi
    }

}
