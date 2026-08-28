# Demo — a minimal, complete integration

A standalone Android app that integrates the Paymentwall SDK the way you will: it consumes the
published `.aar`, from a build that shares nothing with the SDK's own.

Copy `app/build.gradle` and
[`MainActivity.kt`](app/src/main/java/com/example/merchant/MainActivity.kt) as your starting point —
between them they are the whole integration, in about 80 lines.

## Run it

```bash
./gradlew :app:assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```

You need `local.properties` with `sdk.dir` pointing at your Android SDK, or an `ANDROID_HOME`
environment variable.

## What it demonstrates

- **The published `.aar`, plus the four dependencies it cannot declare for itself.** That second
  list is the part to copy carefully: leave one out and the build still succeeds, then fails on the
  payment screen.
  This sample points at the checked-in artifacts under `Core SDK/dist/` and
  `Plugin/MyCard/dist/` so the repository does not carry the same
  binary twice. **In your own app, copy the file into `libs/`** and write
  `implementation files('libs/paymentwall-android-sdk.aar')` — same mechanism, and it is what the
  guide tells you.
- **`minifyEnabled true`.** The SDK's ProGuard rules travel inside the `.aar`, so a minifying
  build needs no keep rules of its own.
- **`google()` and `mavenCentral()` only** — the SDK needs no special repository.
- **An availability report.** It prints `PaymentwallSDK.isAvailable` for every method the SDK ships,
  so you can see exactly what this build can offer. A method whose adapter is not in the build
  reports `false` and is never shown to the payer.
- **Edge-to-edge handled properly.** `targetSdk 35` means the window draws behind the status and
  navigation bars whether you ask for it or not, so `MainActivity` pads by the real inset values
  rather than a guessed number. Skip that and your first line of content is hidden behind the status
  bar — which is worth knowing before you meet it in your own app.

## Two things it is not

- **The keys are placeholders.** Put your own project key in `MainActivity.kt`. With the
  placeholders the SDK reaches the server and the server correctly refuses.
- **The card charge is not implemented.** A real integration registers a `CardChargeHandler` and
  charges the token from its own server; see the
  [Core SDK guide](../Core%20SDK/README.md#step-5-card-payments). This app only launches the flow.

The release build is signed with the **debug** key so the minified APK can be installed and run.
Never ship an app signed that way.
