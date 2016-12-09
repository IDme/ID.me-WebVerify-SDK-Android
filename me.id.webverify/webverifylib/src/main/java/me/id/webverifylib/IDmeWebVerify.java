package me.id.webverifylib;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IDmeWebVerify {
  public static final String IDME_WEB_VERIFY_RESPONSE = "response";
  public static final int WEB_REQUEST_CODE = 39820;

  private static final String ACCESS_TOKEN_KEY = "access_token=";
  private static final String USER_TOKEN_KEY = "user_token";

  private final String IDME_WEB_VERIFY_GET_AUTH_URI = "https://api.id.me/oauth/authorize?client_id=clientID&redirect_uri=redirectURI&response_type=token&scope=scopeType";
  private final String IDME_WEB_VERIFY_GET_USER_PROFILE = "https://api.id.me/api/public/v2/data.json?access_token=user_token";

  private String clientID = "";
  private String redirectURI = "";
  private IDmeScope scope;
  private Activity activity;
  private boolean returnProperties = true;

  /**
   * Constructor For the class.
   *
   * @param clientID    The client ID provided by ID.me http://developer.id.me
   * @param redirectURI The redirect URI
   * @param scope       The Verification type
   * @param activity    The calling activity
   */
  public IDmeWebVerify(String clientID, String redirectURI, IDmeScope scope, Activity activity) {
    this(clientID, redirectURI, scope, activity, false);
  }

  /**
   * Constructor For the class.
   *
   * @param clientID         The client ID provided by ID.me http://developer.id.me
   * @param redirectURI      The redirect URI
   * @param scope            The Verification type
   * @param activity         The calling activity
   * @param returnProperties Whether user properties or access token should be returned
   */
  public IDmeWebVerify(String clientID, String redirectURI, IDmeScope scope, Activity activity, boolean returnProperties) {
    this.scope = scope;
    this.clientID = clientID;
    this.redirectURI = redirectURI;
    this.activity = activity;
    this.returnProperties = returnProperties;
  }

  /**
   * Starts web view activity
   */
  public void startWebView() {
    Intent intent = new Intent(activity, WebViewActivity.class);
    boolean start = true;
    if (clientID == null) {
      Toast.makeText(activity, "Client ID Cannot be null", Toast.LENGTH_SHORT).show();
      start = false;
    }

    if (redirectURI == null) {
      Toast.makeText(activity, "Redirect URI Cannot be null", Toast.LENGTH_SHORT).show();
      start = false;
    }

    if (start) {
      String url = createURL();
      intent.putExtra(WebViewActivity.EXTRA_URL, url);
      intent.putExtra(WebViewActivity.EXTRA_SCOPE, scope);
      intent.putExtra(WebViewActivity.EXTRA_CLIENT_ID, clientID);
      intent.putExtra(WebViewActivity.EXTRA_REDIRECT_URI, redirectURI);
      intent.putExtra(WebViewActivity.EXTRA_RETURN_PROPERTIES, returnProperties);
      activity.startActivityForResult(intent, WEB_REQUEST_CODE);
    }
  }

  /**
   * Creates the url to be loaded in the webView
   *
   * @return URl with redirect uri, client id and scope
   */
  private String createURL() {
    String url = IDME_WEB_VERIFY_GET_AUTH_URI;
    url = url.replace("scopeType", activity.getResources().getString(scope.getKeyRes()));
    url = url.replace("redirectURI", redirectURI);
    url = url.replace("clientID", clientID);
    return url;
  }

  /**
   * Gets Web View Client
   *
   * @return Custom Web View Client
   */
  protected WebViewClient getWebViewClient() {
    return mWebViewClient;
  }

  private WebViewClient mWebViewClient = new WebViewClient() {
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
      boolean hasToken = url.contains(ACCESS_TOKEN_KEY);
      if (hasToken) {
        final String accessToken = extractAccessToken(url);
        if (returnProperties) {
          AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
              getWebProfile(createRequestUrl(accessToken));
              return null;
            }
          };
          asyncTask.execute(null, null, null);
        } else {
          sendDataBack(accessToken);
        }
      }
    }
  };

  /**
   * Creates the URL for the Web Request
   *
   * @return URL with proper formatted request
   */
  private String createRequestUrl(String accessToken) {
    String url = IDME_WEB_VERIFY_GET_USER_PROFILE;
    url = url.replace(USER_TOKEN_KEY, accessToken);
    return url;
  }

  /**
   * Extract Access Token from URL
   *
   * @param url URL that contains access token
   */
  private String extractAccessToken(String url) {
    int firstIndex = url.indexOf("=") + 1;
    int lastIndex = url.indexOf("&");
    return url.substring(firstIndex, lastIndex);
  }

  /**
   * This performs the Web Request
   *
   * @param url URL for the Web Request
   */
  private void getWebProfile(String url) {
    String serverResponse;
    HttpURLConnection urlConnection = null;
    URL urlRequest;
    try {
      urlRequest = new URL(url);
      urlConnection = (HttpURLConnection) urlRequest.openConnection();

      int responseCode = urlConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        serverResponse = readStream(urlConnection.getInputStream());
        sendDataBack(serverResponse);
      }
    } catch (IOException exception) {
      sendErrorBack(exception.getMessage());
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
  }

  /**
   * This converts the InputStream to a String
   *
   * @param inputStream from the Web Request
   * @return the converted string
   */
  private String readStream(InputStream inputStream) throws IOException {
    BufferedReader reader = null;
    StringBuilder response = new StringBuilder();
    try {
      reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException exception) {
          Log.e("Read stream error", exception.getMessage());
        }
      }
    }
    return response.toString();
  }

  /**
   * Sends data back to the activity that called the WebView.
   *
   * @param response The response form the Web Request.
   */
  private void sendDataBack(String response) {
    activity.getIntent().putExtra(IDME_WEB_VERIFY_RESPONSE, response);
    activity.setResult(Activity.RESULT_OK, activity.getIntent());
    activity.finish();
  }

  /**
   * Sends the error back to the activity that called the WebView.
   *
   * @param error The service error form the Web Request.
   */
  private void sendErrorBack(String error) {
    Log.e("Web Request Error", error);
    activity.getIntent().putExtra(IDME_WEB_VERIFY_RESPONSE, error);
    activity.setResult(Activity.RESULT_CANCELED);
    activity.finish();
  }
}
