# GameUI integration instruction

## Step 1
### Android Studio
Import paymentwall-game-ui.aar into your project and add this line in build.gradle:
```java
compile project(':paymentwall-game-ui')
```
### Eclipse
Copy paymentwall-game-ui.jar to 'libs' folder in the project directory

## Step 2
Add a line after UnifiedRequest object initialization
```java
UnifiedRequest request = new UnifiedRequest();
request.setPwProjectKey(Constants.PW_PROJECT_KEY);
request.setPwSecretKey(Constants.PW_SECRET_KEY);
...

request.setUiStyle("game");
```
