FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .build-configuration .build-configuration
COPY gradle gradle
COPY gradlew gradlew
COPY build.gradle.kts settings.gradle.kts gradle.properties versions.toml ./
COPY common common
COPY app app
COPY account account
COPY account-api account-api
COPY contract contract
COPY contract-api contract-api
COPY currency currency
COPY currency-api currency-api
COPY health health
COPY partner partner
COPY partner-api partner-api
COPY pocket pocket
COPY pocket-api pocket-api
COPY transaction transaction
COPY transaction-api transaction-api
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew :app:bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
