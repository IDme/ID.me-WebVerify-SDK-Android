package me.id.webverifylib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.Locale;

public final class IDmeWebVerify {
  private static final String CLIENT_ID_KEY = "clientID";
  private static final String REDIRECT_URI_KEY = "redirectURI";
  private static final String RESPONSE_TYPE_KEY = "responseType";
  private static final String SCOPE_TYPE_KEY = "scopeType";
  private static final String USER_TOKEN_KEY = "user_token";

  private static String idMeWebVerifyGetAuthUri;
  private static String idMeWebVerifyGetUserProfile;

  private static AccessTokenManager accessTokenManager;
  private static String clientID;
  private static String redirectURI = "";
  private static boolean initialized;
  private IDmeGetAccessTokenListener loginGetAccessTokenListener = null;
  private IDmeRegisterAffiliationListener registerAffiliationListener = null;

  private static final IDmeWebVerify INSTANCE = new IDmeWebVerify();

  private IDmePageFinishedListener pageFinishedListener;

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
    idMeWebVerifyGetAuthUri = context.getString(R.string.idme_web_verify_get_auth_uri);
    idMeWebVerifyGetUserProfile = context.getString(R.string.idme_web_verify_get_profile_uri);
    initialized = true;
    accessTokenManager = new AccessTokenManager(context);
    IDmeWebVerify.clientID = clientID;
    IDmeWebVerify.redirectURI = redirectURI;
  }

  private IDmeWebVerify() {

  }

  public static IDmeWebVerify getInstance() {
    return INSTANCE;
  }

  /**
   * Checks if the application is already initialized

   * @throws IllegalStateException Throws exception if the library hasn't been initialized yet
   */
  private void checkInitialization() {
    if (!initialized) {
      throw new IllegalStateException("IDmeWebVerify has to be initialized before use any operation");
    }
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
    checkPendingRequest();

    Intent intent = new Intent(activity, WebViewActivity.class);
    pageFinishedListener = new AuthenticationFinishedListener(this, redirectURI);
    loginGetAccessTokenListener = listener;
    String url = createURL(scope);
    intent.putExtra(WebViewActivity.EXTRA_URL, url);
    intent.putExtra(WebViewActivity.EXTRA_SCOPE_ID, scope.getScopeId());
    activity.startActivity(intent);
  }

  /**
   * Returns an access token
   *
   * @param scope    The type of group verification.
   * @param listener The listener that will be called when the get access token process finished.
   */
  public void getAccessToken(IDmeScope scope, IDmeGetAccessTokenListener listener) {
    checkInitialization();
    AuthToken token = accessTokenManager.getToken(scope);
    if (token == null || !token.isValidToken()) {
      listener.onError(new UnauthenticatedException());
    } else {
      listener.onSuccess(token.getAccessToken());
    }
  }

  /**
   * Returns an access token
   *
   * @param scope       The type of group verification.
   * @param forceReload Force to reload the access token.
   * @param listener    The listener that will be called when the get access token process finished.
   */
  public void getAccessToken(IDmeScope scope, boolean forceReload, IDmeGetAccessTokenListener listener) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the user profile who is associated with that scope
   *
   * @param scope    The type of group verification.
   * @param listener The listener that will be called when the get user profile process finished.
   */
  public void getUserProfile(IDmeScope scope, IDmeGetProfileListener listener) {
    AuthToken token = accessTokenManager.getToken(scope);
    if (token == null) {
      String message = String.format(Locale.US, "There is not an access token related to the %s scope", scope);
      listener.onError(new IllegalStateException(message));
    } else if (token.isValidToken()) {
      String requestUrl = createRequestUrl(token.getAccessToken());
      new GetProfileConnectionTask(listener).execute(requestUrl);
    } else {
      listener.onError(new IllegalStateException("The access token is expired"));
    }
  }

  /** Deletes all session information */
  public void logOut() {
    accessTokenManager.deleteSession();
  }

  /**
   * Deletes all session information regarding to the given scope
   *
   * @param scope The type of group verification.
   */
  public void logOut(IDmeScope scope) {
    accessTokenManager.deleteToken(scope);
  }

  /**
   *
   * @param scope
   * @param affiliationType
   * @param listener
   */
  public void registerAffiliation(Activity activity, IDmeScope scope, IDmeAffiliationType affiliationType, IDmeRegisterAffiliationListener listener) {
    checkInitialization();
    checkPendingRequest();

    Intent intent = new Intent(activity, WebViewActivity.class);
    pageFinishedListener = new RegisterAffiliationFinishedListener(this, redirectURI);
    AuthToken token = accessTokenManager.getToken(scope);
    if (token == null) {
      String message = String.format(Locale.US, "There is not an access token related to the %s scope", scope);
      listener.onError(new IllegalStateException(message));
    } else if (token.isValidToken()) {
      registerAffiliationListener = listener;
      String requestUrl = createRegisterAffiliationUrl(affiliationType, scope, token.getAccessToken());
      intent.putExtra(WebViewActivity.EXTRA_URL, requestUrl);
      intent.putExtra(WebViewActivity.EXTRA_SCOPE_ID, scope.getScopeId());
      activity.startActivity(intent);
    } else {
      listener.onError(new IllegalStateException("The access token is expired"));
    }
  }

  IDmePageFinishedListener getPageFinishedListener() {
    return pageFinishedListener;
  }

  /**
   * Persists the access token for the given scope
   * @param scope
   * @param token
   */
  void saveAccessToken(IDmeScope scope, AuthToken token) {
    accessTokenManager.addToken(scope, token);
  }

  /**
   * Sends access token to the login listener
   */
  void notifyAccessToken(IDmeScope scope, AuthToken token) {
    if (loginGetAccessTokenListener != null) {
      loginGetAccessTokenListener.onSuccess(token == null ? null : token.getAccessToken());
    }
  }

  /**
   * Sends data to the register affiliation listener
   */
  void notifyAffiliationRegistration() {
    if (registerAffiliationListener != null) {
      registerAffiliationListener.onSuccess();
    }
  }

  /**
   * Notifies to any of the available listener about the given error
   */
  void notifyFailure(Throwable throwable) {
    if (loginGetAccessTokenListener!= null) {
      loginGetAccessTokenListener.onError(throwable);
    } else if (registerAffiliationListener != null) {
      registerAffiliationListener.onError(throwable);
    }
  }

  /**
   * Removes the signIn listener
   */
  void clearSignInListener() {
    pageFinishedListener = null;
    loginGetAccessTokenListener = null;
    registerAffiliationListener = null;
  }

  /**
   * Creates the url to be loaded in the webView
   *
   * @return URl with redirect uri, client id and scope
   */
  private String createURL(IDmeScope scope) {
    String url = idMeWebVerifyGetAuthUri;
    url = url.replace(CLIENT_ID_KEY, clientID);
    url = url.replace(REDIRECT_URI_KEY, redirectURI);
    url = url.replace(RESPONSE_TYPE_KEY, "token");
    url = url.replace(SCOPE_TYPE_KEY, scope.getScopeId());
    return url;
  }

  /**
   * Creates the URL for the Web Request
   *
   * @return URL with proper formatted request
   */
  private String createRequestUrl(String accessToken) {
    String url = idMeWebVerifyGetUserProfile;
    url = url.replace(USER_TOKEN_KEY, accessToken);
    return url;
  }

  /**
   * Creates the URL for adding a new affiliation type
   *
   * @return URL with proper formatted request
   */
  private String createRegisterAffiliationUrl(IDmeAffiliationType affiliationType, IDmeScope scope, String accessToken) {
    String url = idMeWebVerifyGetAuthUri;
    url = url.replace(CLIENT_ID_KEY, clientID);
    url = url.replace(REDIRECT_URI_KEY, redirectURI);
    url = url.replace(RESPONSE_TYPE_KEY, "token");
    url = url.replace(SCOPE_TYPE_KEY, affiliationType.getKey());
    return url;
  }

  private void checkPendingRequest() {
    if (loginGetAccessTokenListener != null || registerAffiliationListener != null) {
      throw new IllegalStateException("The activity is already initialized");
    }
  }
}
