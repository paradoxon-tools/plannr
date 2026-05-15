# Plannr Mobile

React Native dashboard prototype for the Plannr server API.

## Stack

- Expo SDK 55 with React Native 0.83 and React 19.2: current stable Expo-managed React Native stack with Android builds available from CI.
- Expo Router: file-based navigation for the dashboard, account drill-down, and pocket detail routes.
- TypeScript: typed DTOs matching the current Kotlin API response shapes.
- AsyncStorage: stores the local server API base URL on the device.

## Run locally

Start the server first:

```bash
cd ../server
./gradlew bootRun
```

Then run the mobile app:

```bash
npm install
npm run android
```

Android emulators use `http://10.0.2.2:8080` by default to reach the host machine. Physical devices usually need your computer's LAN IP, which can be changed in the app's Server screen.

## API coverage

The prototype uses the current server API:

- `GET /accounts`
- `GET /accounts/{id}`
- `GET /contracts?accountId={id}`
- `GET /pockets?accountId={id}`
- `GET /pockets/{id}`

The server currently does not expose a `currentBalance` field on pocket or contract DTOs. The detail screen is wired to show it when that field exists, and otherwise shows that the balance is not exposed by the API.

## APK publishing

`.github/workflows/mobile-apk.yml` builds a release APK on mobile changes and on manual workflow runs. The APK is available as the `plannr-mobile-apk` workflow artifact.

To publish an installable GitHub release:

```bash
git tag mobile-v1.0.0
git push origin mobile-v1.0.0
```

The workflow attaches `plannr-mobile.apk` to the release. In the app, open Server -> Latest GitHub release to download and install the newest APK from GitHub.

This is an [Expo](https://expo.dev) project created with [`create-expo-app`](https://www.npmjs.com/package/create-expo-app).

## Get started

1. Install dependencies

   ```bash
   npm install
   ```

2. Start the app

   ```bash
   npx expo start
   ```

In the output, you'll find options to open the app in a

- [development build](https://docs.expo.dev/develop/development-builds/introduction/)
- [Android emulator](https://docs.expo.dev/workflow/android-studio-emulator/)
- [iOS simulator](https://docs.expo.dev/workflow/ios-simulator/)
- [Expo Go](https://expo.dev/go), a limited sandbox for trying out app development with Expo

You can start developing by editing the files inside the **app** directory. This project uses [file-based routing](https://docs.expo.dev/router/introduction).

## Get a fresh project

When you're ready, run:

```bash
npm run reset-project
```

This command will move the starter code to the **app-example** directory and create a blank **app** directory where you can start developing.

### Other setup steps

- To set up ESLint for linting, run `npx expo lint`, or follow our guide on ["Using ESLint and Prettier"](https://docs.expo.dev/guides/using-eslint/)
- If you'd like to set up unit testing, follow our guide on ["Unit Testing with Jest"](https://docs.expo.dev/develop/unit-testing/)
- Learn more about the TypeScript setup in this template in our guide on ["Using TypeScript"](https://docs.expo.dev/guides/typescript/)

## Learn more

To learn more about developing your project with Expo, look at the following resources:

- [Expo documentation](https://docs.expo.dev/): Learn fundamentals, or go into advanced topics with our [guides](https://docs.expo.dev/guides).
- [Learn Expo tutorial](https://docs.expo.dev/tutorial/introduction/): Follow a step-by-step tutorial where you'll create a project that runs on Android, iOS, and the web.

## Join the community

Join our community of developers creating universal apps.

- [Expo on GitHub](https://github.com/expo/expo): View our open source platform and contribute.
- [Discord community](https://chat.expo.dev): Chat with Expo users and ask questions.
