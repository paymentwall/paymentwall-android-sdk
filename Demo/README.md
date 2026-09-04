# Demo — a minimal, complete integration

A standalone Android app that integrates the Paymentwall SDK the way you will: it resolves the
published artifacts from Maven Central, from a build that shares nothing with the SDK's own.

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

- **Two coordinates, and nothing else.** `paymentwall-android` and, because this sample offers
  MyCard, `paymentwall-android-plugin-mycard`. The SDK brings its own `androidx` and Kotlin
  requirements through its POM — there is no list of transitive dependencies for you to copy and
  keep in step. The versions are in [`gradle.properties`](gradle.properties), one line each, because
  the two artifacts version independently.
- **A `CardChargeHandler`.** The SDK tokenises the card and hands you the token; the charge is yours
  to make server-side. Omit the handler and the payer fills in the whole form and gets
  *"No CardChargeHandler registered"*. This sample approves locally because it has no backend —
  that is the one part not to copy.
- **`minifyEnabled true`.** The SDK's ProGuard rules travel inside the artifact, so a minifying
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
