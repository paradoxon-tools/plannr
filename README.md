# plannr monorepo

This repository is organized as a lightweight monorepo without shared app tooling at the root.

## Apps

- `apps/server` – Spring Boot backend, Gradle wrapper, Dockerfile
- `apps/web` – SvelteKit frontend, npm package manifest, Dockerfile

Docker Compose service names:
- `plannr-server`
- `plannr-web`

## Principles

- App-specific tooling stays inside the app directory
- No root-level `node_modules`
- No root-level Gradle wrapper or build files

## Local development

### Backend

```bash
cd apps/server
./gradlew bootRun
```

### Frontend

```bash
cd apps/web
npm install
npm run dev
```

### Run both with Docker Compose

```bash
docker compose up --build
```

## Docker publishing

The GitHub Actions workflow publishes both app images to GHCR:

- `ghcr.io/paradoxon-tools/plannr-server`
- `ghcr.io/paradoxon-tools/plannr-web`

using `apps/server` and `apps/web` as their respective build contexts.
