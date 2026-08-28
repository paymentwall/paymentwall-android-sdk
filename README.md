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

The SDK ships as an `.aar`. Copy it into your app's `libs/` directory:

- **[Core SDK/dist/paymentwall-android-sdk.aar](Core%20SDK/dist/paymentwall-android-sdk.aar)**
- `SHA256SUMS` sits beside it. Check the digest — a file you were sent is not a file you resolved.

```groovy
dependencies {
    implementation files('libs/paymentwall-android-sdk.aar')

    // Required. An .aar carries no dependency information, so these are yours to
    // declare. Same or newer is fine; these are the versions the SDK is tested against.
    implementation 'androidx.core:core:1.13.1'
    implementation 'androidx.fragment:fragment:1.8.6'
    implementation 'androidx.annotation:annotation:1.9.1'
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:2.0.21'
}
```

⚠️ **Omitting one of those four does not fail your build — it fails at runtime, on the payment
screen.** That is the one real cost of `.aar` distribution, and it goes away when Maven Central
coordinates land in a future release.

A minifying build needs **no keep rules of yours**: the SDK's ProGuard rules travel inside the
`.aar`. [Demo](Demo) is built with minification on if you want to see it.

Full instructions, including the manifest entries and the payment flow:
**[Core SDK integration guide](Core%20SDK/README.md)**

## Payment methods

| Method | What it is | How to add it |
|---|---|---|
| **Local payments** (`PW_LOCAL`) | Paymentwall's hosted page: local methods, bank transfer, cash and wallets, chosen for the payer's country | Built in |
| **Card** (`BRICK`) | Visa, Mastercard, Amex — a native card form, no browser | Built in |
| **Prepaid** (`MINT`) | Vouchers and ePins | Built in |
| **MyCard** (`MYCARD`) | Taiwan prepaid card | A separate `.aar` — [add the adapter](Plugin/MyCard/README.md) |

The first three need nothing beyond the core `.aar`. You choose which to offer per payment, and
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
published `.aar` — the same file you download. It builds with `minifyEnabled true` and resolves
from `google()` and `mavenCentral()` only.

## Support

Questions and integration help: [support@paymentwall.com](mailto:support@paymentwall.com)
