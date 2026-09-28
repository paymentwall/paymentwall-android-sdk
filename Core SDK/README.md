# Core SDK integration guide

Paymentwall Android SDK **2.2**. Everything below is the public API — the package
`com.paymentwall.sdk.api`. You should not need any other package.

- [Step 1: Add the SDK](#step-1-add-the-sdk)
- [Step 2: Register a result callback](#step-2-register-a-result-callback)
- [Step 3: Build a request](#step-3-build-a-request)
- [Step 4: Choose payment methods](#step-4-choose-payment-methods)
- [Step 5: Card payments](#step-5-card-payments)
- [Step 6: Handle the result](#step-6-handle-the-result)
- [Optional: the widget code](#optional-the-widget-code)
- [Optional: two projects in one app](#optional-two-projects-in-one-app)
- [Optional: your own success screen](#optional-your-own-success-screen)
- [Optional: item image](#optional-item-image)
- [Optional: custom pingback parameters](#optional-custom-pingback-parameters)
- [Optional: UI style and theming](#optional-ui-style-and-theming)
- [Diagnostics](#diagnostics)

---

## Step 1: Add the SDK

From Maven Central:

```groovy
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation 'com.paymentwall:paymentwall-android:2.2.0'
}
```

That is the whole dependency. The SDK declares its own `androidx` and Kotlin requirements through
its POM, so there is nothing else for you to add and nothing to keep in step with it.

Optional verification — every artifact is signed:

```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys B730099F96D5462757DFE6B9EE2C59E996E73082
gpg --verify paymentwall-android-2.2.0.aar.asc paymentwall-android-2.2.0.aar
```

### Your manifest needs nothing

The artifact declares its own permissions (`INTERNET`, `ACCESS_NETWORK_STATE`) and its own
activities, and the manifest merger picks them up. **Do not copy activity declarations into your
manifest** — 1.x required that and 2.0 does not.

### Minification

Nothing to add. The SDK's `proguard.txt` travels inside the `.aar`, so a `minifyEnabled true`
build keeps what the SDK needs. [`Demo`](../Demo) is built that way if you want to see it.

### One thing about your own screens, not ours

If your app targets **SDK 35**, Android 15 draws it **edge-to-edge** and no longer honours the
opt-out. Content at the top of your own checkout screen will sit behind the status bar until you pad
for it, and the amount to pad by is not a number you can pick — it changes with the device, a
display cutout and rotation. Ask the window:

```kotlin
ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
    val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
    view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
    insets
}
```

The SDK's own screens already handle this. This note is here because it is the first thing you are
likely to hit while wiring up the screen that *launches* the SDK — [`Demo`](../Demo) shows it in
context.

---

## Step 2: Register a result callback

The SDK is launched through an `ActivityResultContract`, so the result arrives on a callback you
register once — not in `onActivityResult`.

```kotlin
import com.paymentwall.sdk.api.*

class CheckoutActivity : AppCompatActivity() {

    private val payment = registerForActivityResult(PaymentwallContract()) { result ->
        when (result) {
            is PaymentResult.Success    -> onPaid(result.transactionId)
            is PaymentResult.Processing -> reconcileServerSide(result.transactionId)
            is PaymentResult.Cancelled  -> Unit
            is PaymentResult.Failed     -> showError(result.reason, result.message)
        }
    }
}
```

`PaymentResult` is a sealed type, so `when` over it is exhaustive and a new outcome cannot be
silently ignored.

Call this once before you offer a method that ships as a separate adapter:

```kotlin
PaymentwallSDK.initialize()
```

---

## Step 3: Build a request

```kotlin
val request = PaymentRequest.Builder(PROJECT_KEY)
    .amount(BigDecimal("9.99"), "USD")
    .item("sku_001", "500 Gold Coins")
    .user("your_internal_user_id")
    .offer(PaymentMethodId.PW_LOCAL, PaymentMethodId.BRICK)
    .build()

payment.launch(request)
```

| Builder method | Notes |
|---|---|
| `Builder(projectKey)` | Required. Your project key, which is also your public key |
| `.amount(BigDecimal, currency)` | Required. A `Double` overload exists; prefer `BigDecimal` for money |
| `.item(itemId, itemName)` | Required. `itemName` is shown to the payer |
| `.user(userId)` | Required. Your own identifier for the payer |
| `.offer(...)` | Which methods to show. See Step 4 |
| `.secretKey(secretKey)` | Optional; only for methods that sign on the device |
| `.timeoutMillis(ms)` | Optional. How long the SDK waits on the network |
| `.signVersion(n)` | Optional. Leave alone unless asked to change it |
| `.testMode(true)` | Optional. **Does not** switch the card endpoint — see Step 5 |
| `.uiStyle(name)` | Optional. See [UI style](#optional-ui-style-and-theming) |
| `.itemImage(...)` | Optional. See [item image](#optional-item-image) |
| `.customParameter(k, v)` | Optional. Reaches your pingback |
| `.skipSelectionFor(method)` | Optional. Go straight into one method, no selection screen |
| `.widget(code)` | Optional. Which set of methods the hosted checkout offers. See [the widget code](#optional-the-widget-code) |
| `.projectKey(method, key)` | Optional. Pay for one method against a different project. See [two projects in one app](#optional-two-projects-in-one-app) |
| `.showSuccessScreen(false)` | Optional. Return to your app immediately instead of after the SDK's confirmation. See [your own success screen](#optional-your-own-success-screen) |

Amounts are formatted for the currency, so a payer sees `$9.99`, `₫10.000` or `¥1,234` as that
currency is actually written.

---

## Step 4: Choose payment methods

```kotlin
.offer(PaymentMethodId.PW_LOCAL, PaymentMethodId.BRICK, PaymentMethodId.MINT)
```

| Id | Method | Needs |
|---|---|---|
| `PW_LOCAL` | Paymentwall's hosted page — local methods, bank transfer, cash, wallets | Core `.aar` |
| `BRICK` | Card payments, native form | Core `.aar` + a `CardChargeHandler` (Step 5) |
| `MINT` | Prepaid vouchers / ePin | Core `.aar` |
| `MYCARD` | Taiwan prepaid card | [`mycardadapter.aar`](../Plugin/MyCard/README.md) |

Offer exactly what you want the payer to see. With one method the SDK goes straight into it; with
several it shows a selection screen first.

**A method whose adapter you did not ship is not offered.** Ask first if you build variants that
differ:

```kotlin
if (PaymentwallSDK.isAvailable(this, PaymentMethodId.MYCARD)) { /* offer it */ }
```

`isAvailable` answers `true` for `PW_LOCAL`, `BRICK` and `MINT` wherever the SDK is — they are
built in, not adapters. It reports whether a method can **run**, not whether you have finished
configuring it: `BRICK` returns `true` before you have registered a `CardChargeHandler`.

---

## Step 5: Card payments

The SDK never sees your Paymentwall secret and never charges the card itself. It **tokenises** the
card, hands you the token, and waits while your server charges it.

```kotlin
PaymentwallSDK.setCardChargeHandler { card, outcome ->
    // Send card.token to YOUR server, which calls Paymentwall's charge API.
    // Hand back what that API answered, exactly as it came.
    yourApi.charge(card.token, card.secure) { responseBody ->
        outcome.chargeResponse(responseBody)
    }
}
```

`CardToken` carries `token` plus the `cardholderName`, `email` and `fingerprint` the payer
supplied. Your server needs `token`.

| `outcome` call | When |
|---|---|
| `chargeResponse(json)` | **Prefer this.** The charge API's response body, verbatim. The SDK reads it and tells an approval from a refusal from a 3-D Secure challenge by itself |
| `charged()` | The charge succeeded. Pass a permanent token — `charged(permanentToken)` — if your server returned one and you want the SDK to offer that saved card next time |
| `requires3ds(formHtml)` | Your server answered with a 3-D Secure challenge and you picked the value out of the response yourself. **It takes a URL despite the parameter name** — send `secure_return_method=url` with your charge and pass `secure.redirect`. Passing markup loads nothing and the payment stalls |
| `failed(message)` | Declined, or your own error. `message` reaches the payer, so write it for them |

Pass the response body **unwrapped and unmodified**. The SDK reports a body it cannot classify as a
failure rather than guessing at it — so if your backend re-shapes the gateway's answer, use the three
explicit calls instead.

The payment screen stays up until you answer on `outcome`, so answer on **every** path including
your own errors — otherwise the payer waits on a spinner. Exactly one answer takes effect; later
calls are ignored. This is a Java-friendly interface too: implement
`CardChargeHandler.onCardTokenized(card, outcome)`.

### 3-D Secure

Where the issuer supports it, the challenge is drawn **in your app** instead of in a web page. That
costs you one thing: **a payment may take two charges.**

- Send `card.secure?.referenceId` with your charge, as `reference_id`. Without it the gateway cannot
  offer the in-app challenge for that payment, and the payer gets the web page.
- Answer with `chargeResponse(...)`. When the response asks for a challenge, the SDK runs it.
- **Your handler is then called a second time**, with `card.secure?.secureToken` and
  `card.secure?.chargeId` set. Charge again, sending those as `secure_token` and `charge_id`, and
  answer the same way.

`card.secure` is `null` on a payment with no 3-D Secure session, and a challenge shown in a web page
never asks you for a second charge.

Offering `BRICK` without registering a handler is reported as
`PaymentResult.Failed(reason = INVALID_REQUEST)` rather than failing silently.

### Test cards

Card tokenisation goes to Paymentwall's **test** endpoint when your project key starts with `t_`,
and to the live endpoint otherwise. **The `t_` prefix is what selects it — `testMode(true)` does
not.** Use a `t_` project key from your merchant account to test with `4242 4242 4242 4242`.

If tokenisation returns *"Public key is missed or invalid"*, that project does not have card
processing enabled. Enable it in the merchant portal; it is not a code problem.

---

## Step 6: Handle the result

| Result | Meaning | What to do |
|---|---|---|
| `Success` | The payer completed the flow | **Deliver on your server's pingback**, not on this alone. `transactionId` may be null |
| `Processing` | Accepted, not yet confirmed | Reconcile server-side. Neither success nor failure |
| `Cancelled` | The payer backed out | Nothing |
| `Failed` | Declined, or a request that could not work | Show your own message. `reason` and `message` say which |

**You must render your own failure state.** The SDK closes on failure and does not show the payer
an error screen of its own, so if you handle only `Success` the sheet appears to vanish.

`Failed.reason` values:

| Reason | Meaning |
|---|---|
| `INVALID_REQUEST` | The request could never have worked — a missing field, or a method you offered with nothing behind it |
| `DECLINED` | The payment was refused by the processor or the payer's bank |
| `METHOD_UNAVAILABLE` | The chosen method cannot run on this device or in this build |
| `NETWORK` | The request did not reach us, or the answer did not come back |
| `UNKNOWN` | Anything else. `message` is your best information |

---

## Optional: the widget code

A **widget** decides which set of methods the hosted local-payments checkout offers. Its code, e.g.
`pw_1`, is on your project's Widgets page in the Paymentwall merchant area.

```kotlin
.widget("pw_1")
```

**Most integrations should leave it out.** Omit it and Paymentwall offers whatever the project is
configured to offer, so the method list becomes something you change in the merchant area rather
than in an app release. Set it when one app pays through more than one widget — a different mix per
storefront, country or price point.

A widget code belongs to exactly one project, so it travels with the project key it was created
under: change one and you change both.

⚠️ **On a project with no default configured**, omitting the widget produces a page saying the
payment method is not available in the payer's country. That is a message about the missing
parameter, not about the payer. Confirm your project offers something before leaving this out.

---

## Optional: two projects in one app

One app can pay against more than one Paymentwall project — cards on one, local payments on another.

```kotlin
PaymentRequest.Builder(LOCAL_PROJECT_KEY)
    .secretKey(LOCAL_SECRET_KEY)
    .projectKey(PaymentMethodId.BRICK, CARD_PROJECT_PUBLIC_KEY)
```

A method with no entry uses the key the builder was constructed with, so an app with a single
project needs none of this.

**There is no per-method secret key.** Only the hosted local-payments checkout signs on the device,
so `.secretKey(...)` already belongs to that method alone.

> **A card project shows two keys in the merchant area, and only the public one belongs in the
> app.** The private key creates the charge, and that happens on your server — see Step 5. Anything
> compiled into an app can be read out of it.

---

## Optional: your own success screen

```kotlin
.showSuccessScreen(false)
```

This changes **when** you are told, not **whether**. Your result callback still fires exactly once
with the same `PaymentResult` — it simply arrives seconds earlier, while your app is back on screen,
so you can draw your own confirmation from there.

It does not affect the progress indicator or the failure screen. A payer who has just handed over
card details still needs to see that something is happening, and still needs to be told when it did
not work.

---

## Optional: item image

Shown beside the item name on the payment screen.

```kotlin
.itemImage(PaymentRequest.ItemImage.Url("https://example.com/coins.png"))
.itemImage(PaymentRequest.ItemImage.Resource(R.drawable.coins))
.itemImage(PaymentRequest.ItemImage.File(File(cacheDir, "coins.png")))
.itemImage(PaymentRequest.ItemImage.ContentUri("content://media/external/images/media/42"))
```

---

## Optional: custom pingback parameters

Anything you add is echoed to your server's pingback:

```kotlin
.customParameter("campaign", "spring_sale")
```

The widget code is **not** one of these — it has its own setter, `.widget(code)`.

---

## Optional: UI style and theming

Two visual styles ship:

| `uiStyle` | |
|---|---|
| `"v2"` | **Default.** The current design |
| `"saas"` | The 1.x design, for continuity |

```kotlin
.uiStyle("saas")
```

⚠️ **Only those two values are valid.** An unrecognised name has no layouts behind it and the SDK
cannot draw its screens. 1.x documentation mentioned a `"game"` style; **it does not exist** — do
not pass it.

To restyle rather than replace, declare a style named **`PaymentwallSDKTheme`** in your own
`res/values/styles.xml`. Android resource merging gives your app's copy priority, and the SDK's v2
theme inherits from it, so your colours reach both styles:

```xml
<style name="PaymentwallSDKTheme">
    <item name="colorMain">#6750A4</item>
    <item name="textMain">#1B1B1F</item>
    <item name="mainBackground">#FFFBFE</item>
    <!-- ...and every other attribute you rely on. See below. -->
</style>
```

⚠️ **This replaces the SDK's style rather than extending it, so it is closer to all-or-nothing
than it looks.** The theme declares **38 attributes**; anything you leave out no longer has the
SDK's default behind it. Start from the SDK's own `res/values/styles.xml` (open the `.aar` — it is
a zip) and change what you want, rather than writing a short style from scratch.

Both styles honour dark mode via `values-night`, so a payer in dark mode gets a dark payment
screen without you configuring anything. If you override colours, override them for both modes or
your light colours will be used on a dark screen.

---

## Diagnostics

The SDK logs **nothing** by default. While integrating:

```kotlin
SmartLog.setDebugEnabled(true)
```

It prints what the SDK sends, what came back, and why a payment ended as it did. Turn it off
before you ship.
