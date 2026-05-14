plugins {
    id("artifact")
}

dependencies {
    implementation(project(":account-api"))
    implementation(project(":account-shared"))
    implementation(project(":common"))
    implementation(project(":partner-api"))
    implementation(project(":partner-shared"))
    implementation(project(":pocket-api"))
    implementation(project(":pocket-shared"))
    implementation(project(":transaction-materialization-shared"))
    implementation(project(":transaction-projection-shared"))
    implementation(project(":transaction-template-api"))
    implementation(project(":transaction-template-shared"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    runtimeOnly("org.postgresql:r2dbc-postgresql")

    testImplementation(project(":account"))
    testImplementation(project(":partner"))
    testImplementation(project(":pocket"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.flywaydb:flyway-core")
    testImplementation("org.flywaydb:flyway-database-postgresql")
    testImplementation(platform("org.testcontainers:testcontainers-bom:2.0.4"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testRuntimeOnly("org.postgresql:postgresql")
}

sourceSets {
    test {
        resources.srcDir("src/test/resources")
    }
}

tasks.processTestResources {
    from("../app/src/main/resources/db/migration") {
        into("db/migration")
    }
}
