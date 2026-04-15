@file:Suppress("UnstableApiUsage")

import extensions.ApplicationType
import extensions.DatabaseTechnology

plugins {
    id("dericon")

//    https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin
//    id("com.autonomousapps.dependency-analysis") version "1.21.0"
}

subprojects {
    repositories {
        mavenCentral()
    }
}

dericon {

    features {
        applicationType = ApplicationType.WEB
        dbTechnology = DatabaseTechnology.POSTGRES

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
