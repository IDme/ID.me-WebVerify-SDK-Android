#ID.me WebVerify SDK (Android)
The ID.me WebVerify SDK for Android is a class that allows you to verify a user's group affiliation status using ID.me's platform. A sample project has been provided to delineate the integration process.

#Release Information 
- **SDK Version:** 1.0.0 (January 21, 2015)
- **Maintained By:** [Ruben Roman](https://github.com/rubsnick)

For more information please email us at mobile@id.me or visit us at http://developer.id.me.

##Changelog
- Styled Web View Activity


##Installation Instructions
### Gradle
```
repositories
        {
            maven {
                url 'https://oss.sonatype.org/content/groups/public'
            }
        }

dependencies {
    compile 'me.id.webverify:webverifylib:1.1'
}
```

### Manual
Copy the `.aar` file into your project's `libs` folder.

##Setup
Add the following to gradle 

```
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

### Sample Project
To run the sample project you'll need to replace the `clientID` and `redirectUri` values in `MainActivity.java` with those values that are stored withing your registered app at [http://developer.id.me](http://developer.id.me).

## Execution
Both the OAuth flow and verification occur through a WebView that initialized from withn the SDK. Upon successful completion the activity will close and the JSON string will be sent back to the calling activity.

To start the `WebViewActivity` you must initialize a `IDmeWebVerify` object, and call the `StartWebView()`nethod.

    IDmeWebVerify webVerify = new IDmeWebVerify(clientID, redirectURI, affiliationType, activity);
    webVerify.StartWebView();

The params in the constructor are as follows:

- `clientID`: The clientID provided by ID.me when registering the app at [http://developer.id.me](http://developer.id.me).
- `redirectURI`: The redirectURI provided to ID.me when registering your app at [http://developer.id.me](http://developer.id.me)
- `affiliationType`: The type of group verficiation that should be presented. 
- `activity`: The activity that is calling creating the Class Object.

To retrieve the data your `onActivityResult` method will look like the following.

```
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IDmeWebVerify.WEB_REQUEST_CODE)
            {
                String response = data.getStringExtra(IDmeWebVerify.IDME_WEB_VERIFY_RESPONSE);

            }
        }

    }
```

##Results
Each successful request returns the following information:

- Group Affiliation (Military Veteran, Student, Firefighter, etc.)
- Unique user Identifier
- Verification Status

**NOTE:** Other attributes (e.g., email, first name, last name, etc…) can be returned in the JSON results upon special request. Please email [mobile@id.me](mobile@id.me) if your app needs to gain access to more attributes. 

All potential errors that could occur are explained in the next section.

##Error Handling
You will get the raw response from the server when executing. Any runtime errors are handled and it's error message will be passed in the activity result.

## Internet Connectivity
Internet connectivity is required, as the verificaiton occurs through a webView.
