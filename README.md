# plannr server

This repository contains the Spring Boot backend for plannr.

## App

- `apps/server` - Spring Boot backend, Gradle wrapper, Dockerfile

Docker Compose service names:
- `plannr-server`

## Principles

- Server tooling stays inside the app directory
- No root-level `node_modules`
- No root-level Gradle wrapper or build files

## Local development

```bash
cd apps/server
./gradlew bootRun
```

### Run with Docker Compose

```bash
docker compose up --build
```

## Docker publishing

The GitHub Actions workflow publishes the server image to GHCR:

- `ghcr.io/paradoxon-tools/plannr-server`

using `apps/server` as the build context.
