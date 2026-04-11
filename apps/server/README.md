# plannr-server

Coroutine-first Spring Boot backend for plannr.

## Run locally

Prerequisite: Docker Desktop (or another local Docker engine) must be running.

```bash
./gradlew bootRun
```

Spring Boot will automatically start the Postgres service from `compose.yml` and connect the application to it. The same works when you run the app directly from IntelliJ.

For non-local environments, configure the database via the standard Spring properties/environment variables:
- `SPRING_R2DBC_URL`
- `SPRING_R2DBC_USERNAME`
- `SPRING_R2DBC_PASSWORD`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

## Endpoint

```bash
curl http://localhost:8080/health
```

Expected success response:

```text
ok
```

## Build Docker image

```bash
docker build -t plannr-server:local .
```
