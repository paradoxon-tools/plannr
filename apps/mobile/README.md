# Plannr Mobile

Kotlin Multiplatform mobile client for this repository, based on the sibling `plannr-kmp` project.

## Layout

- `bridge`: shared domain and recurrence models used by the client modules
- `client/androidApp`: Android application entry point and packaging module
- `client/compose`: Compose Multiplatform application code for Android, iOS, and desktop
- `client/database`: SQLDelight database and repositories
- `client/iosApp`: Xcode host application for the iOS target

## Run locally

From [`apps/mobile`](D:/Development/chennemann/plannr-server/apps/mobile):

```bash
./gradlew :client:androidApp:assembleDebug
```

For desktop development:

```bash
./gradlew :client:compose:run
```

For Android installs:

```bash
./gradlew :client:androidApp:installDebug
```

## Notes

- This replaces the previous Expo / React Native client.
- The app contents mirror the current working tree of the sibling `..\plannr-kmp` repo as of 2026-08-28.
- Repository-specific domain notes are kept in `CONTEXT.md` and `PROTOTYPE-INSPIRATION.md`.
