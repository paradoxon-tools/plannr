# plannr-server

Coroutine-first Spring Boot backend for plannr.

## Run locally

Prerequisite: Docker Desktop (or another local Docker engine) must be running.

```bash
./gradlew bootRun
```

Spring Boot will automatically start the Postgres service from `compose.yml` and connect the application to it. The same works when you run the app directly from IntelliJ.

For non-local environments, configure the database with one shared set of variables:
- `PLANNR_DB_HOST`
- `PLANNR_DB_PORT`
- `PLANNR_DB_NAME`
- `PLANNR_DB_USERNAME`
- `PLANNR_DB_PASSWORD`

Spring derives both the R2DBC and Flyway JDBC connection settings from these values.

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
