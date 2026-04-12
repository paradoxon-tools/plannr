FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY app app
COPY account-api account-api
COPY contract-api contract-api
COPY currency-api currency-api
COPY partner-api partner-api
COPY pocket-api pocket-api
COPY transaction-api transaction-api
RUN chmod +x gradlew && ./gradlew :app:bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
