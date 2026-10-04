plugins {
    java
    id("com.google.cloud.tools.jib") version "3.5.4"
}

// A separate build keeps the image limited to the tested bootJar and avoids duplicating its dependencies.
val serverJar = file("../../app/build/libs/app.jar")

jib {
    from {
        image = "eclipse-temurin:25-jre"
        platforms {
            platform { architecture = "amd64"; os = "linux" }
        }
    }
    to { image = providers.gradleProperty("imageName").orElse("plannr-server:enable-banking").get() }
    container {
        entrypoint = listOf("java", "-jar", "/app/app.jar")
        workingDirectory = "/app"
        user = "1001:1001"
        ports = listOf("8080")
        environment = mapOf("SERVER_PORT" to "8080", "SPRING_DOCKER_COMPOSE_ENABLED" to "false")
        labels = mapOf(
            "org.opencontainers.image.title" to "Plannr Server",
            "org.opencontainers.image.revision" to providers.gradleProperty("sourceRevision").orElse("local").get(),
        )
    }
    extraDirectories {
        paths {
            path {
                setFrom(serverJar.parentFile)
                into = "/app"
                includes = listOf("app.jar")
            }
        }
    }
}

tasks.named("jibBuildTar") {
    doFirst {
        check(serverJar.isFile) { "Build the server first: ./gradlew :app:bootJar" }
    }
}
