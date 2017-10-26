# Coresdk integration instruction

## Step 1: Add Core SDK
### Android Studio
From your project, create a new module from paymentwall-android-sdk.aar

![](../static/import-coresdk-as.png)

After that add this line in your app module's build.gradle:
```java
compile project(':paymentwall-android-sdk')
```
### Eclipse
Extract paymentwall-android-sdk.zip and import the project into your workspace. And add this as your main project's library.

![](../static/import-coresdk-eclipse.png)

### Declare required permission
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

### Declare required activity
```java
<activity
   android:name="com.paymentwall.pwunifiedsdk.core.PaymentSelectionActivity"
   android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
   android:theme="@style/PaymentwallSDKTheme"
   android:windowSoftInputMode="stateVisible|adjustResize|adjustPan" />
```

## Step 2: Build Unified Payment Params

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
request.setTimeout(30000);
```
``` setTimeout(int timeout)```: set max duration for request (in milliseconds)

``` Set item’s image```: please refer to [guidance](#set-item-image-for-the-request) below on how to set item's image.

### Set item image for the request
There are some data types of an item’s image you can pass to Paymentwall SDK. You can choose one in 4 below options:
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

## Step 3: Add Payment methods

You can choose one or more payment methods according to your needs.
* [Brick](#brick).
* [PWLocal](#pwlocal).
* [Mint](#mint).
* [Mobiamo](#mobiamo).
* [External payment methods](#external-payment-systems-injection).

## Step 4: Set Custom Pingback Parameters (Optional)

### Add custom parameters
By default, Pingback will send ```goodsid``` and ```ref``` to your server. To satisfy the needs for more information, Paymentwall Android Sdk  allows to add extra parameters which can be used for pingback.
Example code:
```java
request.addCustomParam("timeStamp", System.currentTimeMillis() / 1000 + "");
request.addCustomParam("own_order_id", "o123456");
request.addCustomParam("shopname", "ecopark");
```
Multiple parameters are supported.

## Step 5: Launch SDK and Handle Callback

### Launch the SDK
```java
Intent intent = new Intent(getApplicationContext(), PaymentSelectionActivity.class);
intent.putExtra(Key.REQUEST_MESSAGE, request);
startActivityForResult(intent, PaymentSelectionActivity.REQUEST_CODE);
```
### Handle the callback
Paymentwall SDK callback falls in ```onActivityResult(int requestCode, int resultCode, Intent data)```
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

## Step 6: UI Customization (optional)

There are two ways for UI modification which Paymentwall Sdk supports:
* [UI plugins](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Plugin/UIPlugin): add plugin for UI designed by Paymentwall.
* Custom theme: adding several lines of codes in core SDK, the style is determined by you.

### Custom theme
Firstly, create a new style in your styles.xml
```java
<style name="PwsdkCustomTheme" parent="@style/PaymentwallSDKTheme">
</style>
```
with PaymentwallSDKTheme is the default theme for the sdk.

We support the changeable components as in the list below:
```java
        <attr name="colorMain" format="color" />
        <attr name="textMain" format="color" />
        <attr name="mainBackground" format="integer" />
        <attr name="bgProductInfo" format="color" />
        <attr name="bgProductInfoLand" format="color" />
        <attr name="textProductInfo" format="color" />
        <attr name="bgPsButton" format="integer" />
        <attr name="textPsButton" format="color" />
        <attr name="textCopyright" format="color" />
        <attr name="bgInputForm" format="integer" />
        <attr name="bgInputErrorForm" format="integer" />
        <attr name="textInputForm" format="color" />
        <attr name="textInputErrorForm" format="color"/>
        <attr name="iconErrorForm" format="integer"/>
        <attr name="textFormTitle" format="color" />
        <attr name="bgConfirmButton" format="integer" />
        <attr name="textConfirmButton" format="color" />
        <attr name="backButton" format="integer"/>
        <attr name="bgExpDialog" format="color"/>
        <attr name="textExpDialog" format="color"/>
        <attr name="monthButtonNormal" format="integer"/>
        <attr name="monthButtonPressed" format="integer"/>
        <attr name="textMonth" format="color"/>
        <attr name="yearLeftArrow" format="integer"/>
        <attr name="yearRightArrow" format="integer"/>
        <attr name="bgNotifyDialog" format="color"/>
        <attr name="progressColor" format="color"/>
        <attr name="successIcon" format="integer"/>
        <attr name="failIcon" format="integer"/>
        <attr name="textProgress" format="color"/>
        <attr name="textSuccess" format="color"/>
        <attr name="textFail" format="color"/>
        <attr name="textExtraMessage" format="color"/>
        <attr name="iconPwlocal" format="integer" />
```
![](../static/PwsdkTheme.png)

You can modify any component you want by adding it in your custom style.
Example:
```java
<style name="PwsdkCustomTheme" parent="@style/PaymentwallSDKTheme">
        <item name="colorMain">@color/baseRedFont</item>
        <item name="textMain">@color/white</item>
        <item name="mainBackground">@color/pw_secondary_color</item>
        <item name="bgProductInfo">@color/pw_primary_color</item>
        <item name="bgProductInfoLand">@color/pw_primary_color</item>
        <item name="textProductInfo">#FF0000</item>
    </style>
```
Finally, apply the theme for PaymentSelectionActivity in the AndroidManifest.xml:
```java
<activity
      android:name="com.paymentwall.pwunifiedsdk.core.PaymentSelectionActivity"
      android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
      android:windowSoftInputMode="stateVisible|adjustResize|adjustPan"
      android:theme="@style/PwsdkCustomTheme"
      tools:replace="android:theme"/>
```
Declare tools namespace in the manifest tag:
```java
<manifest
    ...  
    xmlns:tools="http://schemas.android.com/tools">
```

## Payment methods integration details

### Brick

**Step 1: Add Brick Payment Method**
```java
request.addBrick();
```
If only Brick is added into `request`, Brick payment will be displayed directly.

NOTE: If you are asked to add bank information in footer, please add code below:

```java
request.enableFooter();
```

**Step 2: Handle One-time Token**

One-time token is automatically obtained by the SDK. You need to register for a broadcast receiver in your activity/service to get the token sent from the sdk:
```java
BroadcastReceiver receiver = new BroadcastReceiver() {
   @Override
   public void onReceive(Context context, Intent intent) {
       if (intent.getAction().equalsIgnoreCase(getPackageName() + Brick.BROADCAST_FILTER_MERCHANT)) {
           String brickToken = intent.getStringExtra(Brick.KEY_BRICK_TOKEN);
           String userEmail = intent.getStringExtra(Brick.KEY_BRICK_EMAIL);
           String cardHolderName = intent.getStringExtra(Brick.KEY_BRICK_CARDHOLDER);
          //process your business logic

       }
   }
};
```
Then you can use the one-time token to create a charge.

**Step 3: Create A Charge**

After obtaining one-time token, send a request from backend to Paymentwall server to create a charge.

POST request to: https://api.paymentwall.com/api/brick/charge

Parameters and description can be referred [here](https://paymentwall.github.io/apis#section-brick-charge).

**Step 4: Handle Charge Response**

```java
Brick.getInstance().setResult(result, token);
```
```result```: 1 or 0 (success or failed)

```token```: the permanent token from charge object you get, if the charge is success.

![](../static/brick-permanent-token.png)

**If the response is ```charge object```**, you need to extract the permanent token and send it back to the SDK:

```java
Brick.getInstance().setResult（1,token);
```

**If the response is ```3d secure form```**, the following is returned in the response body:
```java
{
  "secure":{"formHTML":"..."}
}
```
You need to parse and obtain the 3ds url and then send it back to the sdk:
```java
Brick.getInstance().setResult(form3ds);
```
3ds form will be opened in SDK's native webview. After 3ds is completed, please redirect the form to your website and return a charge object in HTTP response, so SDK can detect the event and close the webview.
3ds webview can also be closed with `Brick.getInstance().setResult(result, token)`

Note: If 3ds is omitted, charge object will be returned in response.

**If the response is an ```error object```**, please set error in the place of token.

```java
Brick.getInstance().setResult(0, errorMessage);
```

**Step 5: Card Scanner Plugin (Optional)**

By compiling CardScanner plugin, users are allowed to use their phone camera to scan credit cards for card number and expired date.
Please refer [here](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Plugin/CardScanner) for integration guidance.

### Mint

Add Mint payment method
```java
request.addMint();
```

### Mobiamo

**Step 1: Declaration**

Declare required permissions in AndroidManifest
```java
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
<permission
   android:name="${applicationId}.mobiamo.PAYMENT_BROADCAST_PERMISSION"
   android:label="Request for sending mobiamobroadcast to Mobiamo"
   android:protectionLevel="signature" />

<uses-permission android:name="${applicationId}.mobiamo.PAYMENT_BROADCAST_PERMISSION" />
```

Declare Mobiamo activity and broadcast receiver
```java
<receiver
   android:name="com.paymentwall.pwunifiedsdk.mobiamo.core.MobiamoBroadcastReceiver"
   android:exported="false"
   android:permission="${applicationId}.mobiamo.PAYMENT_BROADCAST_PERMISSION">
   <intent-filter>
       <action android:name="com.paymentwall.mobiamosdk.SENT_SMS_ACTION"></action>
   </intent-filter>
</receiver>

<activity
   android:name="com.paymentwall.pwunifiedsdk.mobiamo.core.MobiamoDialogActivity"
   android:configChanges="orientation|keyboardHidden|screenSize"
   android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```
**Step 2: Add Mobiamo From UnifiedRequest Object**
```java
request.addMobiamo();
```

### PWLocal

**Step 1: 

```java
<activity
   android:name="com.paymentwall.sdk.pwlocal.ui.PwLocalActivity"
   android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
   android:theme="@android:style/Theme.Translucent" />
```

**Step 2: Add PwLocal Payment Method**

```java
request.addPwLocal();
```
**Step 3: Add Extra parameters**

Use ```addPwlocalParams(String key, String value)``` method to add extra params for Pwlocal.

Some params are automatically added by SDK (taken from UnifiedRequest object you declared above), so you don't need to add them for a second time.
* ```prices```
* ```amount```
* ```currencyCode```
* ```ag_name```
* ```ag_external_id```
* ```uid```

Extra params list can be referred to [here](https://paymentwall.github.io/apis#section-checkout-onetime).

Sample code:
```java
request.addPwlocalParams(Const.P.EMAIL, "fixed");
request.addPwlocalParams(Const.P.WIDGET, "pw");
request.addPwlocalParams(Const.P.EVALUATION, "1");
```

### EXTERNAL PAYMENT SYSTEMS INJECTION
Paymentwall SDK supports external payment system injection (which are in our defined payment system (PS) list). Each time you import an external PS, all you need to do are adding that native sdk (if available) of PS and our adapter (produced for that one) to your project, make the params and then pass to our core Sdk.

**Step 1: Building .gradle  File**

Add compilation lines for external ps sdk and adapter in your main app module build.gradle file
```java
compile project(':pandappsdk')
compile project(':alipayadapter')
compile files('libs/alipaySdk-20160825.jar')
```
**Step 2: Initialzing**

Initialize an external PS and add to UnifiedRequest
```java
public ExternalPs(String id , String displayName, int iconResId, Serializable params)
```

```id```: payment system’s id

```displayName```: label of the ps displayed on the button.

```iconResId```: PS logo resource id

```Params```: parameters object passed to core sdk (varies among different PS).

**Step 3: Add Payment Options**

Add external ps object to UnifiedRequest object.

```java
request.add(ps1, ps2,...);
```
