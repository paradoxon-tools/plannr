# plannr-server

Coroutine-first Spring Boot backend for plannr.

## Run locally

```bash
./gradlew bootRun
```

Default local database: in-memory H2 via R2DBC.

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
