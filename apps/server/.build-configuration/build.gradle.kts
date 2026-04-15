plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:kotlin-allopen:${libs.versions.kotlin.get()}")
    implementation("org.jetbrains.kotlin:kotlin-noarg:${libs.versions.kotlin.get()}")

    implementation("org.springframework.boot:spring-boot-gradle-plugin:${libs.versions.gradle.plugin.spring.get()}")
}
