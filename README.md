# ID.me WebVerify SDK (Android)
The ID.me WebVerify SDK for Android is a library that allows you to verify a user's group affiliation status using 
ID.me's platform. A sample project has been provided to delineate the integration process.

## Specification
### Supported Android Versions

Android API 16 (Jellybean) and above is supported.

## Release Information 
- **SDK Version:** 2.0.0 (March 28, 2017)
- **Maintained By:** [ID.me](http://github.com/IDme)

For more information please email us at mobile@id.me or visit us at http://developer.id.me.

## Changelog
- The changelog can be found in [CHANGELOG.md](me.id.webverify/CHANGELOG.md)
 
## Installation Instructions
### Gradle

Add a library dependency to your app module's `build.gradle`:

```groovy
dependencies {
    compile 'me.id.webverify:webverifylib:2.0.0'
}
```

You'll need to have the Sonatype repository in your list of repositories

```groovy
repositories {
    maven {
        url 'https://oss.sonatype.org/content/groups/public'
    }
}

```

### Manual
Copy the last stable version `.aar` file into your project's `libs` folder.
You can get it from the [maven repository](https://repo1.maven.org/maven2/me/id/webverify/webverifylib/) or you can compile the project.

Add the following to gradle 

```groovy
repositories {
    flatDir {
        dirs 'libs'
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile(name: 'IDmeWebVerify', ext: 'aar')
}
```

## Setup
### Step 1
You have to declare you redirect url in your application `build.gradle` file.
In order to do that, you have to add the following code replacing `CUSTOM_SCHEME` with the redirect custom scheme inside `android.defaultConfig` block.


```groovy
android {
    // ...
    defaultConfig {
        // ...,
        manifestPlaceholders = [
            idmeAuthRedirectScheme: "CUSTOM_SCHEME"
        ]
    }
    // ...
}
```
For example: if your redirectUri is `my_custom_scheme://callback`, replace `CUSTOM_SCHEME` with `my_custom_scheme`.

### Step 2
You must call `IDmeWebVerify.initialize(Context appContext, String clientId, String clientSecret, String redirectUri)` before using the SDK. 
This method should only be called once. It should be called in your Application initialization method (`onCreate` method) or if you haven't defined an Application class, you can invoke it in your `MainActivity` initialization method.

The params in the initializer are as follows:
- `appContext`: The application context.
- `clientId`: The clientId provided by I.me when registering the app at [http://developer.id.me](http://developer.id.me).
- `clientSecret`: The clientSecret provided by ID.me when registering the app at [http://developer.id.me](http://developer.id.me).
- `redirectUri`: The redirectUri provided to ID.me when registering your app at [http://developer.id.me](http://developer.id.me).


## Execution
Both the OAuth flow and verification occur through a device browser that initialized from within the SDK following the modern OAuth [best practices for native apps](https://tools.ietf.org/html/draft-ietf-oauth-native-apps-03).
The SDK provides a bunch of functions that will use a callback parameter to send back the result of them. 

### Login
In order to login a user in the ID.me system you have to use one of the following functions.

```java
IDmeWebVerify.getInstance().login(@NonNull Activity activity, @NonNull IDmeScope scope, @NonNull IDmeGetAccessTokenListener listener)
```

or 
```java
IDmeWebVerify.getInstance().login(@NonNull Activity activity, @NonNull IDmeScope scope, @Nullable LoginType loginType, @NonNull IDmeGetAccessTokenListener listener) 
```

Both functions are similar, the only difference is that in the second one you can specify the login type.

Parameters:
- `activity` The activity that is invoking the login process. It'll be used to call the native browser. 
- `scope` The scope of the login. 
- `loginType` The loginType specify whether it's a sign in or sign up process.
- `iDmeGetAccessTokenListener` The iDmeGetAccessTokenListener is used to retrieve the access token after the login process is finished.

### Access Token
The SDK handles the access and refresh tokens by storing them in the device. As the access tokens are short-lived it can happen quite frequently that the stored access token has expired and needs to be refreshed. 
That is why you have to call `IDmeWebVerify.getInstance().getAccessToken()` which includes a callback with the token (refreshed if it had expired) or an error. You should call this method before each of your requests to the ID.me backend.

### Get user profile
In order to get the user profile you have to call `IDmeWebVerify.getInstance().getUserProfile()` function

### Results and Error Handling
All results will be passed in the functions callbacks.

### Internet Connectivity
Internet connectivity is required, as the verification occurs through the device browser.

## Sample Project Setup

- To run the sample project you'll need to replace the `clientId`, `secretId` and `redirectUri` values in `MainActivity.java` with those values that are stored withing your registered app at [http://developer.id.me](http://developer.id.me).
- Also you need to register your `redirectUri` schema in the sample gradle application file.

    ```groovy
    manifestPlaceholders = [
        idmeAuthRedirectScheme: "CUSTOM_SCHEME"
    ]
    ```
- Replace the app scope in the [Scope class](me.id.webverify/app/src/main/java/me/id/meidwebverify/Scope.java) with the one defined for the application. 

## License
```   
  The MIT License (MIT)
  
  Copyright (c) 2015 ID.me
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
```