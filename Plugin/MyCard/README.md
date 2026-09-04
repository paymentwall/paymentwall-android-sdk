# MyCard integration

MyCard (Taiwan prepaid card) is the one payment method that ships as a separate artifact. Add it
only if you offer MyCard; the core SDK does not depend on it.

## Step 1: add the plugin

```groovy
dependencies {
    implementation 'com.paymentwall:paymentwall-android:2.0.1'
    implementation 'com.paymentwall:paymentwall-android-plugin-mycard:2.0.1'
}
```

The plugin declares the core as a dependency, so the second line alone is enough — the first is
there because you want it explicit in your build file.

**It carries its own version number.** MyCard changes far less often than the SDK, so the core may
be ahead of it; use the latest of each rather than trying to match them.

There is no setup call. The plugin registers itself when it is on the classpath.

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
