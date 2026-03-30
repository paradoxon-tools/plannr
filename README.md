# plannr-server

Minimal coroutine-first Spring Boot application with a single endpoint:

- `GET /health` returns `ok` when the database connection is established
- returns HTTP `503` when the database is unavailable

## Run locally

```bash
./gradlew bootRun
```

Default local database: in-memory H2 via R2DBC.

## Run with Docker Compose

```bash
docker compose up --build
```

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
