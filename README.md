# paymentwall-android-sdk

## Introduction
Do you want to accept payments from mobile users in different countries, different payment system by writing just few of code lines? 
Paymentwall is a global mobile payment gateway that accepts payments from more than 200 countries with 100+ alternative payment options. We now provide SDK for Android which will become a native part of your application, it eliminates the necessity to open a web browser for payments. Less steps, faster process, there’s no doubt your conversion rate will get boost! All you have to do is import the library into your project and config it to start accepting in-app payment. It is quick and easy! We'll guide you through the process here.


## HOW DOES IT WORK?
1. Compile the library url or add jar to your project. 
      With different areas, we provide corresponding external payment system jar files. You can add as many as you want. You can also enable/disable default payment options too. You can add any payment option as they want by importing the payment system library and library adapter provided by Paymentwall to their project
2. User requests a purchase inside your application.
3. Paymentwall SDK initializes payment screen with 3 core payment options (Brick, MINT, Mobiamo) and the other is “Local Payments” option. 
4. User initiates payment in-app 
      With Brick, Mint, Mobiamo the payment process will totally be native.
      With local payments, local payment screen will be shown with payment methods corresponding to user’s current location. Here users can then select a payment option they prefer.
      
## REQUIREMENTS
Android 4.0.1 (API level 14) and above.

## CREDENTIALS
Paymentwall SDK integration requires a project key. Obtain these Paymentwall API credentials in the application settings of your Merchant Account at paymentwall.com

## ADD CORE SDK
Paymentwall SDK is delivered as a JAR package or public repository. Drop the jar file into your project or add a compile line into your gradle build file. 

### Declare required permission
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<permission
   android:name="${applicationId}.mobiamo.PAYMENT_BROADCAST_PERMISSION"
   android:label="Request for sending mobiamobroadcast to Mobiamo"
   android:protectionLevel="signature" />

<uses-permission android:name="${applicationId}.mobiamo.PAYMENT_BROADCAST_PERMISSION" />
```
On android 6.0 and above, you have to request user these permissions explicitly: android.permission.SEND_SMS, android.permission.READ_SMS, android.permission.READ_PHONE_STATE.

### Declare required activity
```java
<activity
   android:name="com.paymentwall.pwunifiedsdk.core.PaymentSelectionActivity"
   android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
   android:windowSoftInputMode="stateVisible|adjustResize|adjustPan" />
```
### Declare Mobiamo broadcast receiver
```java
<receiver
   android:name="com.paymentwall.pwunifiedsdk.mobiamo.core.MobiamoBroadcastReceiver"
   android:exported="false"
   android:permission="${applicationId}.mobiamo.PAYMENT_BROADCAST_PERMISSION">
   <intent-filter>
       <action android:name="com.paymentwall.mobiamosdk.SENT_SMS_ACTION"></action>
   </intent-filter>
</receiver>
```
### Import needed classes to your app activity
```java
import com.paymentwall.pwunifiedsdk.core.PaymentSelectionActivity;
import com.paymentwall.pwunifiedsdk.core.UnifiedRequest;
import com.paymentwall.pwunifiedsdk.util.Key;
import com.paymentwall.pwunifiedsdk.util.ResponseCode;
```
### Define the request from your activity
```java
UnifiedRequest request = new UnifiedRequest();
```
### Set payment params
```java
request.setPwProjectKey(Constants.PW_PROJECT_KEY);
request.setPwSecretKey(Constants.PW_SECRET_KEY);
request.setAmount(good.getPrice());
request.setCurrency(good.getCurrency());
request.setItemName(good.getName());
request.setItemId(good.getId());
request.setUserId(Constants.USER_ID);
request.setSignVersion(3);
request.setItemResID(good.getImage());
request.setNativeDialog(true);
request.setTimeout(30000);
```
``` setNativeDialog(boolean enable) ```: if true, indicate that you want BrickSdk to display successful or failed payment dialog, otherwise, you want to display yourself.

``` setTimeout(int timeout)```: set max duration for brick http request (in milliseconds)

Set item’s image: Refer this

### Add Brick payment method
```java
request.addBrick();
```
### Brick payment flow
When brick token is successfully obtained inside the sdk, a broadcast intent will be sent to merchant’s app (This part may confuse merchant, we should remove it or give it a special style to let merchant know that this part belongs to our SDK, it’s not needed to be done by merchant side):
```java
Intent intent = new Intent();
intent.setAction(getActivity().getPackageName() + Brick.BROADCAST_FILTER_MERCHANT);
intent.putExtra(Brick.KEY_BRICK_TOKEN, token);
LocalBroadcastManager.getInstance(self).sendBroadcast(intent);
```
Register for a broadcast receiver in your activity/service to get the token sent from the sdk:
```java
BroadcastReceiver receiver = new BroadcastReceiver() {
   @Override
   public void onReceive(Context context, Intent intent) {
       if (intent.getAction().equalsIgnoreCase(getPackageName() + Brick.BROADCAST_FILTER_MERCHANT)) {
           String brickToken = intent.getStringExtra(Brick.KEY_BRICK_TOKEN);
          //process your business logic

       }
   }
};
```
```java
LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(getPackageName() + Brick.BROADCAST_FILTER_MERCHANT));
```
If you choose to display payment result dialog by the sdk, after processing your business logic, you need to send a broadcast intent back to the sdk with the result (successful or failed)
```java
int backendResult = 1; //1 means your processing is successful, 0 is failed
Intent intent = new Intent();
intent.setAction(getPackageName() + Brick.BROADCAST_FILTER_SDK);
intent.putExtra(Brick.KEY_MERCHANT_SUCCESS, backendResult);
LocalBroadcastManager.getInstance(MerchantBackendService.this).sendBroadcast(intent);
```
### Add Mint payment method
```java
request.addMint();
```
### Add Mobiamo payment method
*Note: to use mobiamo payment, you need to gain privilege from integration team.
```java
request.addMobiamo();
```
### Set item image for the request
There are some data types of an item’s image you can pass to Panda++ sdk. You can choose one in 4 below options:
```java
//From url
setItemUrl(String itemUrl);

//From file
setItemFile(File itemFile);

//From resource
setItemResID(int itemResID);

//Content provider(image's URI converted to string)
setItemContentProvider(String itemContentProvider);
```
### Launch the SDK
```java
Intent intent = new Intent(getApplicationContext(), PaymentSelectionActivity.class);
intent.putExtra(Key.REQUEST_MESSAGE, request);
startActivityForResult(intent, PaymentSelectionActivity.REQUEST_CODE);
```
### Handle the callback
Panda++ sdk callback falls in ```onActivityResult(int requestCode, int resultCode, Intent data)```
Result code is one of those constants in ResponseCode class.
```java
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
   case ResponseCode.MERCHANT_PROCESSING:
       // This case is only for Brick. If nativeDialog set to false,
       // means that merchant displays successful payment dialog by himself
       // so the sdk will return brick token and this resultCode to merchant app
       break;
   default:
       break;
}
```

## EXTERNAL PAYMENT SYSTEMS INJECTION
Panda++ supports external payment system injection (which are in our defined payment system (PS) list). Each time you import an external PS, all you need to do are adding that native sdk (if available) of PS and our adapter (produced for that one) to your project, make the params and then pass to our core Sdk.

Add compilation lines for external ps sdk and adapter in your main app module build.gradle file
```java
compile project(':pandappsdk')
compile project(':alipayadapter')
compile files('libs/alipaySdk-20160825.jar')
```
### Initialize an external PS and add to UnifiedRequest
```java
public ExternalPs(String id , String displayName, int iconResId, Serializable params)
```
```id```: payment system’s id, as defined in this.
```displayName```: label of the ps displayed on the button.
```iconResId```: PS logo resource id.,
```Params```: parameters object passed to core sdk (varies among different PS).
```java
request.add(ps1, ps2,...);
```

