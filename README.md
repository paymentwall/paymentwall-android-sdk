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
- [See CoreSDK integration instruction](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Core%20SDK/README.md)

## THIRD PARTY PAYMENT METHOD PLUGINS
- [See Plugin injection guide](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Core%20SDK/README.md#external-payment-systems-injection)
### List of available plugins
- [Alipay](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Plugin/Alipay)
- [Wechatpay](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Plugin/Wechatpay)
- [MyCard](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Plugin/Mycard)
- [Credit card scanner](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Plugin/CardScanner)
- [UI Plugin](https://github.com/paymentwall/paymentwall-android-sdk/tree/master/Plugin/UIPlugin)

