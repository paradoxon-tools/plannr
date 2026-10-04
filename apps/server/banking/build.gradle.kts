plugins { id("artifact") }

dependencies {
    implementation(project(":common"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.postgresql:r2dbc-postgresql")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.flywaydb:flyway-core")
    testImplementation("org.flywaydb:flyway-database-postgresql")
    testRuntimeOnly("org.postgresql:postgresql")
}

tasks.processTestResources {
    from("../app/src/main/resources/db/migration") { into("db/migration") }
}
