package me.id.webverifylib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public final class IDmeWebVerify {
  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String EXPIRE_TOKEN_KEY = "expires_in";
  private static final String USER_TOKEN_KEY = "user_token";

  private final String IDME_WEB_VERIFY_GET_AUTH_URI = "https://api.id.me/oauth/authorize?client_id=clientID&redirect_uri=redirectURI&response_type=token&scope=scopeType";
  private final String IDME_WEB_VERIFY_GET_USER_PROFILE = "https://api.id.me/api/public/v2/data.json?access_token=user_token";

  private static AccessTokenManager accessTokenManager;
  private static String clientID;
  private static String redirectURI = "";
  private static boolean initialized;
  private IDmeGetAccessTokenListener loginGetAccessTokenListener = null;

  private static final IDmeWebVerify INSTANCE = new IDmeWebVerify();

  /**
   * This method needs to be called before IDmeWebVerify can be used.
   * Typically it will be called from your Application class's onCreate method.
   *
   * @param context Application context
   */
  public static void initialize(Context context, String clientID, String redirectURI) {
    if (initialized) {
      throw new IllegalStateException("IDmeWebVerify is already initialized");
    }
    if (clientID == null) {
      throw new IllegalStateException("ClientId cannot be null");
    }
    if (redirectURI == null) {
      throw new IllegalStateException("RedirectURI cannot be null");
    }
    initialized = true;
    accessTokenManager = new AccessTokenManager(context);
    IDmeWebVerify.clientID = clientID;
    IDmeWebVerify.redirectURI = redirectURI;
  }

  /**
   * Constructor For the class.
   */
  private IDmeWebVerify() {

  }

  public static IDmeWebVerify getInstance() {
    return INSTANCE;
  }

  /**
   * Starts the login process
   *
   * @param activity which will be used to start the login activity
   * @param scope    The type of group verification.
   * @param listener The listener that will be called when the login process is finished.
   */
  public void login(Activity activity, IDmeScope scope, IDmeGetAccessTokenListener listener) {
    checkInitialization();
    Intent intent = new Intent(activity, WebViewActivity.class);
    if (loginGetAccessTokenListener != null) {
      throw new IllegalStateException("The activity is already initialized");
    }

    loginGetAccessTokenListener = listener;
    String url = createURL(activity, scope);
    intent.putExtra(WebViewActivity.EXTRA_URL, url);
    intent.putExtra(WebViewActivity.EXTRA_SCOPE, scope);
    activity.startActivity(intent);
  }

  public void getAccessToken(IDmeScope scope, IDmeGetAccessTokenListener listener) {
    checkInitialization();
    AuthToken token = accessTokenManager.getToken(scope);
    if (token == null || !token.isValidToken()) {
      listener.onError(new UnauthenticatedException());
    } else {
      listener.onSuccess(token.getAccessToken());
    }
  }

  public void getAccessToken(IDmeScope scope, boolean forceReload, IDmeGetAccessTokenListener listener) {
    throw new UnsupportedOperationException();
  }

  public void getUserProfile(IDmeGetProfileListener listener) {
    checkInitialization();
    // TODO mirland 13/12/16:
  }

  public void logOut() {
    checkInitialization();
    // TODO mirland 13/12/16:
  }

  /**
   * Checks if the application is already initialized
   * @throws IllegalStateException
   */
  private void checkInitialization() {
    if (!initialized) {
      throw new IllegalStateException("IDmeWebVerify has to be initialized before use any operation");
    }
  }

  /**
   * Creates the url to be loaded in the webView
   *
   * @return URl with redirect uri, client id and scope
   */
  private String createURL(Context context, IDmeScope scope) {
    String url = IDME_WEB_VERIFY_GET_AUTH_URI;
    url = url.replace("scopeType", context.getResources().getString(scope.getKeyRes()));
    url = url.replace("redirectURI", redirectURI);
    url = url.replace("clientID", clientID);
    return url;
  }

  /**
   * Checks if the URL has a correct AccessToken and saves it if it is right
   *
   * @return if the access token was saved correctly
   */
  boolean validateAndSaveAccessToken(String url, IDmeScope scope) {
    if (url.contains(ACCESS_TOKEN_KEY)) {
      AuthToken authToken = extractAccessToken(url);
      accessTokenManager.addToken(scope, authToken);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Send access token to the login listener
   */
  void notifyAccessToken(IDmeScope scope) {
    AuthToken token = accessTokenManager.getToken(scope);
    if (loginGetAccessTokenListener != null) {
      loginGetAccessTokenListener.onSuccess(token == null ? null : token.getAccessToken());
    }
  }

  /**
   * Remove the signIn listener
   */
  void clearSignInListener() {
    loginGetAccessTokenListener = null;
  }

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
  private AuthToken extractAccessToken(String url) {
    // the service url does not respect the rfc3986, the query parameters start with "#" and it should start with "?"
    // https://tools.ietf.org/html/rfc3986
    Uri uri = Uri.parse(url.replace("#", "?"));
    AuthToken authToken = new AuthToken();
    authToken.setAccessToken(uri.getQueryParameter(ACCESS_TOKEN_KEY));
    try {
      Integer expirationInMinutes = Integer.valueOf(uri.getQueryParameter(EXPIRE_TOKEN_KEY));
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MINUTE, expirationInMinutes);
      authToken.setExpiration(calendar);
    } catch (NumberFormatException ex) {
      ex.printStackTrace();
    }
    return authToken;
  }

  /**
   * This performs the Web Request
   *
   * @param url URL for the Web Request
   */
  // TODO mirland 14/12/16:
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
        // TODO mirland 13/12/16: notify data
      }
    } catch (IOException exception) {
      // TODO mirland 13/12/16: notify error
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
}
