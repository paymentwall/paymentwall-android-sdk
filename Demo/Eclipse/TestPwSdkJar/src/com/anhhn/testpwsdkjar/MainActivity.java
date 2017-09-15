package com.anhhn.testpwsdkjar;

import java.util.TreeMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;

import com.paymentwall.alipayadapter.PsAlipay;
import com.paymentwall.pwunifiedsdk.brick.core.Brick;
import com.paymentwall.pwunifiedsdk.core.PaymentSelectionActivity;
import com.paymentwall.pwunifiedsdk.core.UnifiedRequest;
import com.paymentwall.pwunifiedsdk.object.ExternalPs;
import com.paymentwall.pwunifiedsdk.util.Key;
import com.paymentwall.pwunifiedsdk.util.MiscUtils;
import com.paymentwall.pwunifiedsdk.util.ResponseCode;
import com.paymentwall.sdk.pwlocal.message.CustomRequest;
import com.paymentwall.sdk.pwlocal.ui.PwLocalActivity;
import com.paymentwall.sdk.pwlocal.utils.ApiType;

public class MainActivity extends Activity {

	private Button btnStart;

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equalsIgnoreCase(
					getPackageName() + Brick.BROADCAST_FILTER_MERCHANT)) {
				processBackend(intent.getStringExtra(Brick.KEY_BRICK_TOKEN),
						intent.getStringExtra(Brick.KEY_BRICK_EMAIL));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startPayment();
			}
		});

		LocalBroadcastManager.getInstance(this).registerReceiver(
				receiver,
				new IntentFilter(getPackageName()
						+ Brick.BROADCAST_FILTER_MERCHANT));
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
		super.onDestroy();
	}

	private void startPayment() {
		UnifiedRequest request = new UnifiedRequest();

		request.setPwProjectKey(Constants.PW_PROJECT_KEY);
		request.setPwSecretKey(Constants.PW_SECRET_KEY);

		request.setAmount(0.99);
		request.setCurrency("USD");
		request.setItemName("Test item");
		request.setItemId("testitem1");
		request.setUserId("testuser1");
		request.setItemResID(R.drawable.ic_launcher);
		request.setTimeout(30000);
		request.setSignVersion(3);

		request.setTestMode(false);

		request.addBrick();
		request.addMint();
		request.addMobiamo();

		PsAlipay alipay = new PsAlipay();
		alipay.setAppId("external");
		alipay.setPaymentType("1");
		alipay.setItbPay("30m");
		alipay.setForexBiz("FP");
		alipay.setAppenv("system=android^version=3.0.1.2");

		ExternalPs alipayPs = new ExternalPs("alipay", "Alipay", 0, alipay);

		request.add(alipayPs);

		Intent intent = new Intent(getApplicationContext(),
				PaymentSelectionActivity.class);
		intent.putExtra(Key.REQUEST_MESSAGE, request);
		startActivityForResult(intent, PaymentSelectionActivity.REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent dkata) {

		switch (resultCode) {
		case ResponseCode.ERROR:
			// There is an error with the payment
			break;
		case ResponseCode.CANCEL:
			// User cancels the payment
			break;
		case ResponseCode.SUCCESSFUL:
			// The payment is successful
			break;
		case ResponseCode.FAILED:
			// The payment was failed
			break;
		default:
			break;
		}
	}

	private void processBackend(final String token, final String email) {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				int backendResult = 1; // 1 means your processing is successful,
										// 0 is failed
				Brick.getInstance().setResult(backendResult,
						"DummyPermanentToken");
			}
		}, 2000);

	}
}
