# Changelog

## 2.2.0

**An in-app 3-D Secure challenge, two projects in one app, and a flatter checkout.** Nothing you
have already written stops working: everything below is either invisible or opt-in.

```groovy
implementation 'com.paymentwall:paymentwall-android:2.2.0'
implementation 'com.paymentwall:paymentwall-android-plugin-mycard:2.0.1'   // only if you offer MyCard
```

### Added

- **A 3-D Secure challenge is now drawn in your app** rather than in a web page, wherever the issuer
  supports it. Where they do not, the web-page flow is unchanged.

  An in-app challenge means the payment takes **two charges**: charge the token as usual, and when
  your handler is called a second time with `CardToken.secure.secureToken` set, charge again sending
  `secure_token` and `charge_id`. Your charge should also send `reference_id` from
  `CardToken.secure.referenceId` whenever it is set — without it the gateway cannot offer an in-app
  challenge for that payment.
- **`ChargeOutcome.chargeResponse(json)`** — hand back the charge API's response body verbatim and
  the SDK reads it, telling an approval from a refusal from a 3-D Secure challenge by itself.
  **Prefer it over `charged()` / `requires3ds()` / `failed()`**, which each carry one field you had
  to pick out of the response yourself and cannot express a native challenge.
- **`CardToken.secure`** — the three values a 3-D Secure charge needs: `referenceId`, `secureToken`
  and `chargeId`. Null on a payment with no 3-D Secure session.
- **`PaymentRequest.Builder.projectKey(method, key)`** — pay for one method against a different
  Paymentwall project, e.g. cards on one project and local payments on another. A method with no
  entry uses the key the builder was constructed with, so a single-project app needs none of it.
- **`PaymentRequest.Builder.widget(code)`** — the widget code is a first-class setter now.
  `.customParameter("widget", …)` still works and still means the same thing; where you use both,
  the setter wins. Still optional, and still best left out unless one app pays through more than one
  widget.
- **`PaymentRequest.Builder.showSuccessScreen(false)`** — your result callback fires as soon as the
  payment succeeds, instead of after the SDK's confirmation countdown, so your app can draw its own
  confirmation. It is called exactly once either way, with the same result; it simply arrives
  earlier. The progress and failure screens are unaffected. Default `true`.

### Changed

- **The checkout is flat.** Methods that used to sit behind a nested list are rows on the first
  screen, so a payer sees everything on offer at once and reaches any of it in one tap.
- **"Local Payments"** is what the hosted-page method is called, on the row and on the screen it
  opens. It was inconsistent before.
- **The payment screens link to Paymentwall's privacy policy** from their footer, so a payer can
  read it before they pay.
- A selected row now shows a chevron rather than a check mark, matching the iOS SDK.

### Fixed

- **`ChargeOutcome.requires3ds` takes a URL, despite the parameter being named `formHtml`.** The
  documentation said it took markup; the behaviour has not changed. Send
  `secure_return_method=url` with your charge and pass the response's `secure.redirect` — passing
  markup loads nothing and the payment stalls with no error. The parameter keeps its name because
  renaming it would break anyone calling it with a named argument. `chargeResponse(json)` handles
  both shapes and is the better answer.
