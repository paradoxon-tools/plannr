package internal

import extensions.config
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.jetbrains.kotlin.plugin.jpa")
}

kotlin {
    jvmToolchain {
        languageVersion.set(config.versions.javaLanguage.map { JavaLanguageVersion.of(it) })
    }
    compilerOptions {
        apiVersion.set(KOTLIN_1_9)
    }
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}

java {
    toolchain {
        languageVersion.set(config.versions.javaLanguage.map { JavaLanguageVersion.of(it) })
    }
}
