# plannr-server

Coroutine-first Spring Boot backend for plannr.

## Bank connections

[Enable Banking setup and API guide](docs/enable-banking.md) covers connecting personal bank accounts, linking existing planning accounts, importing actual transactions, and reconciling planned occurrences. Executable requests are in the **Banking** Bruno folder.

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
curl http://localhost:9000/actuator/health
```

Expected success response:

```json
{"status":"UP"}
```

## Build Docker image

```bash
docker build -t plannr-server:local .
```

## Deployment templates

Example Watchtower deployment templates for test and production live in
`deploy/watchtower`.
