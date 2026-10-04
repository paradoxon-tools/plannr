# Deploy the banking server image

GitHub Actions builds the server Dockerfile after the server tests pass and publishes it to GHCR. Banking integration tests use a disposable PostgreSQL service. The image contains no banking key or runtime credentials.

## Publish through GitHub Actions

Run **Build and push Docker images** on `feat/enable-banking` with `image_tag=enable-banking`, or use:

```bash
gh workflow run ghcr.yml --ref feat/enable-banking -f image_tag=enable-banking
```

The workflow publishes `ghcr.io/paradoxon-tools/plannr-server:enable-banking` and a `sha-<commit>` tag. Pin the SHA tag or image digest for repeatable deployments. Pushes to `develop` and `master` retain their existing publication behavior.

## Optional local archive build

From `apps/server`:

```bash
./gradlew test :app:bootJar
./gradlew -p deploy/image jibBuildTar -PimageName=plannr-server:enable-banking
```

The Docker-loadable archive is `deploy/image/build/jib-image.tar`. Jib also writes its image digest and ID next to the archive. The existing Dockerfile remains available for normal Docker builds. To record a source revision, pass `-PsourceRevision=<commit-id>` to the archive build.

## Deploy on a Docker host

Copy this directory to the deployment host. Log in to GHCR if the package requires authentication. Place your PKCS#8 RSA key at `private-key.pem`, then:

```bash
cp .env.example .env
# Edit .env with your database password, application ID and registered HTTPS callback.
# Make the mounted key readable only by the container user on a Linux host:
sudo chown 1001:1001 private-key.pem
sudo chmod 400 private-key.pem
docker compose --env-file .env -f compose.yml config --quiet
docker compose --env-file .env -f compose.yml pull
docker compose --env-file .env -f compose.yml up -d
curl --fail http://localhost:8080/actuator/health
```

The default port binding is localhost for use behind your authenticated HTTPS reverse proxy. Configure `PLANNR_BIND_ADDRESS` if your network arrangement requires another binding. Register the exact public `/banking/callback` URL with Enable Banking.

This standalone stack creates a new PostgreSQL volume. For an existing deployment, pull the GHCR image and use `PLANNR_IMAGE` as its image reference; retain its existing database configuration and volume. Add the three `ENABLE_BANKING_*` runtime variables and a read-only key mount, following `compose.yml`. Back up the existing database before the first startup, which applies migrations V44 and V45. Do not replace an existing database volume with this new stack's empty volume.

After startup, follow [the banking API guide](../../docs/enable-banking.md) and use Bruno to connect and link accounts. Imports and reconciliation remain on demand. Live-bank access requires your own registered application and consent.

The local archive is an optional offline fallback; the deployment instructions use the image built and published by GitHub Actions.
