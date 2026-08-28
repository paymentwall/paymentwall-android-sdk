package com.example.merchant

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.paymentwall.sdk.api.PaymentMethodId
import com.paymentwall.sdk.api.PaymentRequest
import com.paymentwall.sdk.api.PaymentResult
import com.paymentwall.sdk.api.PaymentwallContract
import com.paymentwall.sdk.api.PaymentwallSDK

/**
 * A minimal, complete Paymentwall integration.
 *
 * Everything the SDK needs is here: register a result callback, build a
 * [PaymentRequest], launch it, and handle the typed [PaymentResult].
 */
class MainActivity : AppCompatActivity() {

    private lateinit var out: TextView

    // Register once. The result arrives here rather than in onActivityResult.
    private val payment = registerForActivityResult(PaymentwallContract()) { result ->
        out.text = when (result) {
            is PaymentResult.Success -> "Success (txn ${result.transactionId ?: "none"})"
            is PaymentResult.Processing -> "Processing - reconcile server-side"
            is PaymentResult.Cancelled -> "Cancelled by the payer"
            is PaymentResult.Failed -> "Failed: ${result.reason} ${result.message ?: ""}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PaymentwallSDK.initialize()

        out = TextView(this).apply { setPadding(32, 32, 32, 32) }

        // isAvailable tells you whether a method can run in THIS build before you offer
        // it to a payer. The three built-in methods are always available; a method that
        // ships as a separate .aar reports false when that file is not included.
        val report = buildString {
            appendLine("Adapter availability, as the SDK reports it:")
            listOf(
                PaymentMethodId.PW_LOCAL, PaymentMethodId.BRICK, PaymentMethodId.MINT,
                PaymentMethodId.MYCARD
            ).forEach { id ->
                appendLine(
                    "  ${id.value.padEnd(10)} -> ${
                        PaymentwallSDK.isAvailable(
                            this@MainActivity,
                            id
                        )
                    }"
                )
            }
        }

        val pay = Button(this).apply {
            text = "Pay (pwlocal + brick + mycard)"
            setOnClickListener {
                val request = PaymentRequest.Builder("MERCHANT_PROJECT_KEY")
                    .secretKey("MERCHANT_SECRET_KEY")
                    .customParameter(
                        "widget",
                        "pw_1"
                    ) // Use the correct widget type defined on the merchant portal
                    .amount(java.math.BigDecimal("9.99"), "USD")
                    .item("sku_001", "A test item")
                    .user("merchant_test_user")
                    .offer(PaymentMethodId.PW_LOCAL, PaymentMethodId.BRICK, PaymentMethodId.MYCARD)
                    .build()
                payment.launch(request)
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = report
                setPadding(32, 32, 32, 16)
                tag = "availability"
            })
            addView(pay)
            addView(out)
        }

        // targetSdk 35 means Android 15 draws this window edge-to-edge, behind the
        // status and navigation bars, so content at the top of the screen is covered
        // until something pads for it.
        //
        // Ask the window how much the bars actually cover rather than picking a padding
        // value: it changes with the device, a display cutout and rotation.
        val root = ScrollView(this).apply { addView(content) }
        setContentView(root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // The system paints the clock and battery icons in one colour, so on a light
        // background they need to be dark to be visible at all.
        val lightBackground =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) !=
                Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, root).apply {
            isAppearanceLightStatusBars = lightBackground
            isAppearanceLightNavigationBars = lightBackground
        }
    }
}
