# MyCard integration

MyCard (Taiwan prepaid card) is the one payment method that ships as a separate file. Add it only
if you offer MyCard; the core SDK does not depend on it.

## Step 1: add the adapter

Copy [`dist/mycardadapter.aar`](dist) into your app's `libs/` directory, beside the core
`.aar`. Verify it against `dist/SHA256SUMS` the same way:

```groovy
dependencies {
    implementation files('libs/paymentwall-android-sdk.aar')
    implementation files('libs/mycardadapter.aar')
    // ...plus the four core dependencies from the Core SDK guide
}
```

There is no setup call. The adapter registers itself when it is on the classpath.

## Step 2: offer it

```kotlin
val request = PaymentRequest.Builder(PROJECT_KEY)
    .amount(BigDecimal("9.99"), "USD")
    .item("sku_001", "500 Gold Coins")
    .user("your_internal_user_id")
    .offer(PaymentMethodId.PW_LOCAL, PaymentMethodId.MYCARD)
    .build()

payment.launch(request)
```

That is the whole integration. **No `PsMyCard` object and no `ExternalPs` row** — 1.x required you
to construct a method payload and add it to the request by hand, and 2.0 does not: you name the
method and the SDK does the rest.

## Checking it is there

```kotlin
PaymentwallSDK.isAvailable(context, PaymentMethodId.MYCARD)
```

`true` when the `.aar` is in the build, `false` when it is not. If it is missing, the method is
simply **not offered** to the payer — no row appears, and nothing fails in front of them. Useful
if you ship build variants that differ.

## Notes

- MyCard opens its own screen and returns to your app when the payer finishes. The result reaches
  you through the same `PaymentResult` callback as every other method.
- The adapter contributes nothing to your manifest beyond what the core SDK already declares.
