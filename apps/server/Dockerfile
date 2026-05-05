ARG GRADLE_VERSION=9.4.1

FROM gradle:${GRADLE_VERSION}-jdk25 AS build
WORKDIR /workspace

COPY --chown=gradle:gradle . .

RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle :app:bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app

RUN useradd --system --create-home --uid 1001 appuser

COPY --from=build /workspace/app/build/libs/app.jar /app/app.jar

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
