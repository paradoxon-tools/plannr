plugins {
    id("org.springframework.boot") version "4.0.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("jvm") version "2.3.20" apply false
    kotlin("plugin.spring") version "2.3.20" apply false
}

allprojects {
    group = "de.chennemann.plannr"
    version = "1.0.0"
}

subprojects {
    repositories {
        mavenCentral()
    }
}
