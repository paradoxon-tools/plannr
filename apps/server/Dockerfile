FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY app/build.gradle.kts app/build.gradle.kts
COPY app/src app/src
RUN chmod +x gradlew && ./gradlew :app:bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
