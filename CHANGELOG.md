# Changelog

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
