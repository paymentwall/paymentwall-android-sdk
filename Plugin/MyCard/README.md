# MyCard integration instruction

## Step 1
### Android Studio
Import mycardadapter-release.aar to your project, add this line to build.gradle file
```java
compile project(':mycardadapter-release')
```
### Eclipse
Copy mycardadapter-release.jar to 'libs' folder in the project directory.

## Step 2
Initialize an instance of PsMyCard object (after UnifiedRequest object initialization)
```java
UnifiedRequest request = new UnifiedRequest();
request.setPwProjectKey(Constants.PW_PROJECT_KEY);
request.setPwSecretKey(Constants.PW_SECRET_KEY);
...

PsMyCard myCard = new PsMyCard();
```

## Step 3
Create an instance of ExternalPs with the above PsMyCard object
```java
ExternalPs myCardPs = new ExternalPs("mycard", "MyCard", R.drawable.ps_logo_mycard, myCard);
```
Add this to the unified request object:
```java
request.add(myCardPs);
```
