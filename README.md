# paymentwall-android-sdk

Accept payments inside your Android app. Paymentwall is a global payment gateway reaching more
than 200 countries with 100+ alternative payment options, and this SDK becomes a native part of
your application — so a payer never leaves it for a browser.

**Version 2.0** is a substantial rewrite of 1.x: a new public API, a new payment UI, AndroidX
throughout, and support for modern Android. If you are integrating for the first time, start at
[Add the SDK](#add-the-sdk).

The payment screens are native, follow the payer's light or dark mode, and format the total the way
the currency is actually written in the payer's region. Build [Demo](Demo) to see them.

## Requirements

| | |
|---|---|
| **minSdk** | 24 (Android 7.0) |
| **compileSdk / targetSdk** | 35 |
| **Java bytecode** | 1.8 — your app does not need a newer toolchain |
| **Kotlin** | Not required. The API is usable from Java; the samples are Kotlin |

## How it works

1. Add the SDK to your app.
2. Your app builds a `PaymentRequest` and launches it.
3. The SDK shows a payment screen offering the methods **you** listed, takes the payment, and
   hands back a typed result.
4. Your server confirms the payment through Paymentwall's pingback. **The pingback is the
   authoritative record** — see [Confirming a payment](#confirming-a-payment).

## Credentials

You need a **project key** and a **secret key**, both from the application settings of your
Merchant Account at [paymentwall.com](https://api.paymentwall.com/developers/applications).

**The project key is also your public key.** The card form uses that same value, and there is no
separate card credential to configure. If card payments come back *"Public key is missed or
invalid"*, that project does not have card processing enabled — enable it in the merchant portal.

The secret key is **optional**, needed only by the payment methods that still sign on the device.
Anything in your APK can be extracted from it, so treat a secret that has shipped inside an app
binary as compromised for any other purpose.

## Add the SDK

Two lines, from Maven Central:

```groovy
dependencies {
    implementation 'com.paymentwall:paymentwall-android:2.0.1'

    // ONLY if you offer MyCard. Omit it and the method reports itself unavailable.
    implementation 'com.paymentwall:paymentwall-android-plugin-mycard:2.0.1'
}
```

Requires `mavenCentral()` and `google()` in your repositories — the `androidx` artifacts come from
Google's, as they do for every Android project. **Nothing else is needed:** the SDK declares its own
`androidx` and Kotlin requirements, and adding the MyCard artifact alone pulls the core in with it.

A minifying build needs **no keep rules of yours**: the SDK's ProGuard rules travel inside the
artifact. [Demo](Demo) is built with minification on if you want to see it.

The published binary is obfuscated. Its `-sources.jar` is a notice rather than source code; the API
reference is the `-javadoc.jar`, which your IDE picks up automatically.

**The two carry independent version numbers.** The MyCard plugin changes far less often than the
SDK, so the core may be at `2.1.0` while the plugin is still `2.0.1` — a supported pair, not a
mistake. The plugin declares the SDK as a *minimum*, so Gradle resolves whichever core you asked
for. Use the latest of each; you do not need to match them.

### Coming from 2.0.0

**2.0.0 was a file you copied into `libs/`. That channel is retired** — 2.0.1 and everything after
it is published to Maven Central only, and no further `.aar` files are cut. Two things change:

```groovy
// before
implementation files('libs/paymentwall-android-sdk.aar')
implementation 'androidx.core:core:1.13.1'
implementation 'androidx.fragment:fragment:1.8.6'
implementation 'androidx.annotation:annotation:1.9.1'
implementation 'org.jetbrains.kotlin:kotlin-stdlib:2.0.21'

// after
implementation 'com.paymentwall:paymentwall-android:2.0.1'
```

**Delete those four dependency lines.** They existed only because a file carries no dependency
information — omitting one built fine and then failed at runtime, on the payment screen. The POM
carries them now, at the versions the SDK is tested against.

**And the artifact is renamed.** `paymentwall-android-sdk` → `paymentwall-android`, and
`mycardadapter` → `paymentwall-android-plugin-mycard`: the coordinates drop the redundant `-sdk` and
put every add-on under a `-plugin-` prefix. Nothing about the code or the API changed with the name.

**Verifying the release.** Every artifact is signed. Fetch the key from `keyserver.ubuntu.com`:

```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys B730099F96D5462757DFE6B9EE2C59E996E73082
gpg --verify paymentwall-android-2.0.1.aar.asc paymentwall-android-2.0.1.aar
```

Full instructions, including the manifest entries and the payment flow:
**[Core SDK integration guide](Core%20SDK/README.md)**

## Payment methods

| Method | What it is | How to add it |
|---|---|---|
| **Local payments** (`PW_LOCAL`) | Paymentwall's hosted page: local methods, bank transfer, cash and wallets, chosen for the payer's country | Built in |
| **Card** (`BRICK`) | Visa, Mastercard, Amex — a native card form, no browser | Built in |
| **Prepaid** (`MINT`) | Vouchers and ePins | Built in |
| **MyCard** (`MYCARD`) | Taiwan prepaid card | A separate artifact — [add the plugin](Plugin/MyCard/README.md) |

The first three need nothing beyond the core artifact. You choose which to offer per payment, and
the SDK only ever shows what you asked for.

Ask `PaymentwallSDK.isAvailable(context, id)` before offering a method whose adapter you may not
have shipped. A method you have not included is simply **not offered** to the payer — it is never
drawn as a row that fails when tapped.

## Confirming a payment

`PaymentResult.Success` means the payer completed the flow on the device. **It is not proof of a
settled transaction**, and `Success.transactionId` may be null on methods that report no id to the
device. Deliver goods on your server's pingback, not on the device result alone.

`PaymentResult.Processing` is neither success nor failure: the payment was accepted and is not yet
confirmed. Reconcile it server-side.

## Diagnostics

The SDK is **silent by default**. To see what it is doing while you integrate:

```kotlin
SmartLog.setDebugEnabled(true)
```

Turn it off before you ship.

## Sample app

**[Demo](Demo)** is a complete, minimal integration written only against the
published artifact — the same one you resolve. It builds with `minifyEnabled true` and resolves
from `google()` and `mavenCentral()` only.

## Support

Questions and integration help: [support@paymentwall.com](mailto:support@paymentwall.com)
