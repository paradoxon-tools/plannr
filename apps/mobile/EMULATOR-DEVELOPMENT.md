# Emulator backend

Android debug builds running on an emulator use `http://localhost:9000`.
Physical phones, release builds and iOS retain the normal backend URL.

Forward the emulator's port to the computer before launching the app:

```sh
adb -s emulator-5554 reverse tcp:9000 tcp:9000
```

Run the local server on port 9000. Reapply forwarding after restarting the emulator.
Cleartext HTTP is allowed only for localhost in the Android debug manifest.

Wallet → management gear → Partners provides the partner list and editor.
Partner names/descriptions use Save changes; logo previews must be accepted with
Use this logo. No website or domain is persisted. Accounts and pockets management
are listed as planned sections and do not open incomplete editors.
