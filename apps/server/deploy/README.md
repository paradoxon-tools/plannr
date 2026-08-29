# Watchtower deployment

This directory contains a Watchtower-based deployment layout for the server.

It matches the repository's existing release model:

- `develop` publishes the mutable `ghcr.io/paradoxon-tools/plannr-server:develop`
  tag for the test environment.
- Release publication keeps immutable `v<major>.<minor>` tags and also updates
  the mutable `ghcr.io/paradoxon-tools/plannr-server:stable` tag for
  production.

That gives you two channels:

- `test` auto-updates from `develop`
- `production` auto-updates only when a GitHub release is published, because
  only the release workflow moves `stable`

## Files

- `watchtower/docker-compose.test.yml` runs the test stack from `:develop`
- `watchtower/docker-compose.prod.yml` runs the production stack from `:stable`
- `watchtower/test.env.example` shows the required test runtime variables
- `watchtower/prod.env.example` shows the required production runtime variables

## How it works

Watchtower only updates a running container when the container's configured
image reference changes in the registry. That means:

- test follows `ghcr.io/paradoxon-tools/plannr-server:develop`
- production follows `ghcr.io/paradoxon-tools/plannr-server:stable`
- production does not follow version tags directly

The release workflow is responsible for publishing both:

- `ghcr.io/paradoxon-tools/plannr-server:v<major>.<minor>`
- `ghcr.io/paradoxon-tools/plannr-server:stable`

## Host setup

1. Copy the `watchtower` directory to the Linux host.
2. Create `test.env` or `prod.env` from the matching example file.
3. Log the host into GHCR:

   ```bash
   echo "$GHCR_READ_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin
   ```

4. Start the desired environment:

   ```bash
   docker compose --env-file test.env -f docker-compose.test.yml up -d
   docker compose --env-file prod.env -f docker-compose.prod.yml up -d
   ```

## Notes

- Watchtower is intentionally scoped to the `plannr-server` container via
  labels and container name arguments.
- Test and production use separate Watchtower scopes so both can run on the
  same Docker host safely.
- Production still benefits from immutable version tags for rollback; to roll
  back, retag the previous release image as `stable` or temporarily pin the
  production stack to a specific `v<major>.<minor>` tag.
