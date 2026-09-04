# Changelog

## 2.0.1

**The SDK is on Maven Central, and that is now the only channel.**

```groovy
implementation 'com.paymentwall:paymentwall-android:2.0.1'
implementation 'com.paymentwall:paymentwall-android-plugin-mycard:2.0.1'   // only if you offer MyCard
```

**No further `.aar` files are cut**, and the ones 2.0.0 shipped are no longer in this repository.
See *Coming from 2.0.0* in the [README](README.md) — it is two lines of change, and it **deletes**
the four dependencies you previously had to declare by hand, because the POM carries them now.

**The artifacts are renamed.** `paymentwall-android-sdk` → `paymentwall-android`, and
`mycardadapter` → `paymentwall-android-plugin-mycard`. The coordinates drop the redundant `-sdk` and
put every add-on under a `-plugin-` prefix. Nothing about the code or the API changed with the name.

**The two version independently.** MyCard changes far less often than the SDK, so the core may be at
a higher version than the plugin. Use the latest of each; they do not have to match.

### One API change

`PaymentRequest.Builder.user(userId)` is **required**, and `build()` now rejects a request without
it, naming the field. It was always required in practice — the SDK refused such a request further
in, after the payer had already chosen a payment method, with a message that named nothing. If you
already call `.user(...)`, as the guides have always shown, nothing changes for you.

### Verifying a release

Every artifact is signed. The key is `B730099F96D5462757DFE6B9EE2C59E996E73082`, on
`keyserver.ubuntu.com`.


## 2.0.0

A rewrite of the integration surface. If you are on 1.x, expect to change code — the payment
screens, the request object and the result handling are all different.

### You will need to change

- **New API.** `com.paymentwall.sdk.api`: build a `PaymentRequest`, launch it through
  `PaymentwallContract`, handle a typed `PaymentResult`. `UnifiedRequest`,
  `PaymentSelectionActivity`, `Key` and `ResponseCode` are no longer the integration surface.
- **Results arrive on an `ActivityResultContract` callback**, not `onActivityResult`.
- **Card payments use a `CardChargeHandler`.** 1.x broadcast the card token over
  `LocalBroadcastManager` with a custom permission and you replied by broadcasting back. That is
  gone: you get a direct call and answer on a `ChargeOutcome`.
- **`minSdk` is 24** (was 14).
- **Your manifest no longer declares the SDK's activities or permissions.** The `.aar` declares
  its own. Remove the copies 1.x told you to add.
- **Payment methods are named, not constructed.** `.offer(PaymentMethodId.MYCARD)` replaces
  building a `PsMyCard` and adding an `ExternalPs` row by hand.

### New

- A redesigned payment UI, with **dark mode**. Pass `.uiStyle("saas")` to keep the 1.x look.
- **Currency-correct totals.** Amounts are formatted the way the currency is written in its own
  region, rather than always in US convention.
- **Accessibility**: content labels, field associations, and support for large font scales.
- `PaymentwallSDK.isAvailable(context, id)` tells you whether a method can run before you offer it.
- `SmartLog.setDebugEnabled(true)` — the SDK is silent by default and this is how you see inside it
  while integrating.

### Removed

- **Mobiamo** (carrier billing), the **card scanner** (its library was archived upstream in 2021),
  and the **`"game"` UI plugin**. 1.x documentation described a `setUiStyle("game")` — it never had
  layouts behind it in this distribution and must not be used.
- **Alipay, WeChat Pay and UnionPay adapters are not published in 2.0.** The code has not been
  abandoned; it is simply not part of this release, and a later version may carry it. If you offered
  one of these through 1.x, contact support before upgrading.

### Fixed

Many correctness fixes in the payment paths, including several that could report a wrong outcome to
your app. Of note for integrators:

- A method you did not include is **not offered** to the payer. It used to be drawn as a row that
  failed when tapped, after the payer had chosen it.
- `isAvailable` used to answer `false` for the SDK's own built-in methods, including cards — so
  following the documented "check before you offer" advice concluded that card payments were
  unavailable.
- The hosted checkout could end on the browser's error page after a **successful** payment.
- A successful payment now always reports to your app, on every path that can reach it.

### Distribution

2.0.0 ships as an `.aar` you copy into `libs/`, with `SHA256SUMS` beside it. Maven Central
coordinates are planned; until then an `.aar` carries no dependency information, so the four
AndroidX/Kotlin dependencies listed in the [Core SDK guide](Core%20SDK/README.md) are yours to
declare.
