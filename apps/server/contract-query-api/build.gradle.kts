plugins {
    id("io.spring.dependency-management")
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation(project(":transaction-query-api"))
    implementation("org.springframework:spring-web")
}
