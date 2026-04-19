# plannr-server

Coroutine-first Spring Boot backend for plannr.

## Run locally

Prerequisite: Docker Desktop (or another local Docker engine) must be running.

```bash
./gradlew bootRun
```

Spring Boot will automatically start the Postgres service from `compose.yml` and connect the application to it. The same works when you run the app directly from IntelliJ.

The local Compose Postgres container is exposed on host port `15432` by default to avoid conflicts with an existing local Postgres on `5432`. Override it with `PLANNR_DB_HOST_PORT` for Docker Compose and `PLANNR_DB_PORT` for the application if needed.

For non-local environments, configure the database with one shared set of variables:
- `PLANNR_DB_HOST`
- `PLANNR_DB_PORT`
- `PLANNR_DB_NAME`
- `PLANNR_DB_USERNAME`
- `PLANNR_DB_PASSWORD`

Spring derives both the R2DBC and Flyway JDBC connection settings from these values.

## Endpoint

```bash
curl http://localhost:8080/actuator/health
```

Expected success response:

```json
{"status":"UP"}
```

## Development seed data

With the `local` Spring profile active, the development seed endpoint is enabled by default:

```bash
curl -X POST http://localhost:8080/internal/dev/seed
```

Set `PLANNR_DEV_SEED_ENABLED=false` to disable it for a local run.

## Build Docker image

```bash
docker build -t plannr-server:local .
```
