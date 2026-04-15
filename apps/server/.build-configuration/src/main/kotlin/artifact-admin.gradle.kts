import extensions.DatabaseTechnology.MONGO
import extensions.DatabaseTechnology.POSTGRES
import extensions.config

plugins {
    id("internal.common")
}

dependencies {
    implementation(config.features.dbTechnology.map {
        when (it) {
            MONGO -> "org.springframework.boot:spring-boot-starter-data-mongodb-reactive"
            POSTGRES -> "org.springframework.boot:spring-boot-starter-data-r2dbc"
        }
    })
}