# Wechatpay integration instruction

## Step 1
Add this line to your app module’s build.gradle file:
```java
compile project(':wechatpayadapter-release')
```
      
## Step 2
Initialize an instance of PsWechat object (after UnifiedRequest object initialization)
```java
UnifiedRequest request = new UnifiedRequest();
request.setPwProjectKey(Constants.PW_PROJECT_KEY);
request.setPwSecretKey(Constants.PW_SECRET_KEY);
...

PsWechat wechat = new PsWechat();
wechat.setTradeType("APP");
```

## Step 3
Create an instance of ExternalPs with the above PsWechat object
```java
ExternalPs wechatPs = new ExternalPs("wechat", "Wechatpay", R.drawable.ps_logo_wechat_pay, wechat);
```
Add this to the unified request object:
```java
request.add(wechatPs);
```

## Step 4
Update your WeChat credential according to WeChat's policy 
[https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5](https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_5)
Make sure that your package name and package signature are set up properly.