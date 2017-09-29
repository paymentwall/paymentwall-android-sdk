package com.paymentwall.testpandalib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.paymentwall.alipayadapter.PsAlipay;
import com.paymentwall.pwunifiedsdk.core.PaymentSelectionActivity;
import com.paymentwall.pwunifiedsdk.core.UnifiedRequest;
import com.paymentwall.pwunifiedsdk.object.ExternalPs;
import com.paymentwall.pwunifiedsdk.util.Key;
import com.paymentwall.pwunifiedsdk.util.ResponseCode;
import com.paymentwall.sdk.pwlocal.utils.Const;

/**
 * Created by andy.ha on 9/14/2016.
 */

public class MainActivity extends Activity {

    private Button btnStart;

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

        Intent intent = new Intent(this, BrickService.class);
        startService(intent);
    }

    private void startPayment() {
        UnifiedRequest request = new UnifiedRequest();
        request.setPwProjectKey(Constants.PW_PROJECT_KEY);
        request.setPwSecretKey(Constants.PW_SECRET_KEY);

        request.setAmount(Constants.AMOUNT);
        request.setCurrency(Constants.CURRENCY);
        request.setItemName(Constants.ITEM_NAME);
        request.setItemId(Constants.ITEM_ID);
        request.setUserId(Constants.USER_ID);
        request.setItemResID(R.mipmap.ic_launcher);
        request.setTimeout(30000);
        request.setSignVersion(3);

        request.setTestMode(false);

        request.addBrick();
        request.addMint();
        request.addMobiamo();

        request.addPwLocal();
        request.addPwlocalParams(Const.P.EMAIL, "fixed");
        request.addPwlocalParams(Const.P.WIDGET, "pw");
        request.addPwlocalParams(Const.P.EVALUATION, "1");


        PsAlipay alipay = new PsAlipay();
        alipay.setAppId(Constants.ALIPAY.APP_ID);
        alipay.setPaymentType(Constants.ALIPAY.PAYMENT_TYPE);
        // extra params for international account
        alipay.setItbPay(Constants.ALIPAY.IT_B_PAY);
        alipay.setForexBiz(Constants.ALIPAY.FOREX_BIZ);
        alipay.setAppenv(Constants.ALIPAY.APPENV);

        ExternalPs alipayPs = new ExternalPs("alipay", "Alipay", 0, alipay);
        request.add(alipayPs);

        Intent intent = new Intent(getApplicationContext(), PaymentSelectionActivity.class);
        intent.putExtra(Key.REQUEST_MESSAGE, request);
        startActivityForResult(intent, PaymentSelectionActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
}
