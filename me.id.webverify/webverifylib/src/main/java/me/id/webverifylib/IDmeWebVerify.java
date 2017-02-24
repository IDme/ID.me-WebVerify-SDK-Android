package me.id.webverifylib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

import me.id.webverifylib.exception.UnauthenticatedException;
import me.id.webverifylib.listener.IDmeAccessTokenManagerListener;
import me.id.webverifylib.listener.IDmeGetAccessTokenListener;
import me.id.webverifylib.listener.IDmeGetProfileListener;
import me.id.webverifylib.listener.IDmePageFinishedListener;
import me.id.webverifylib.listener.IDmeRegisterAffiliationListener;
import me.id.webverifylib.listener.IDmeRegisterConnectionListener;
import me.id.webverifylib.listener.IDmeScope;
import me.id.webverifylib.listener.RegisterAffiliationFinishedListener;
import me.id.webverifylib.listener.RegisterConnectionFinishedListener;
import me.id.webverifylib.networking.GetProfileConnectionTask;

public final class IDmeWebVerify {
  private static final String CLIENT_ID_KEY = "clientID";
  private static final String REDIRECT_URI_KEY = "redirectURI";
  private static final String RESPONSE_TYPE_KEY = "responseType";
  private static final String SCOPE_TYPE_KEY = "scopeType";
  private static final String SIGN_TYPE_KEY = "signType";
  private static final String USER_TOKEN_KEY = "user_token";

  private static String idMeWebVerifyGetAccessTokenUri;
  private static String idMeWebVerifyGetAuthUri;
  private static String idMeWebVerifyGetUserProfile;

  private static AccessTokenManager accessTokenManager;
  private static RefreshAccessTokenHandler refreshAccessTokenHandler;
  private static String clientId;
  private static String redirectUri = "";
  private static String secretId = "";
  private static boolean initialized;

  private IDmeGetAccessTokenListener loginGetAccessTokenListener = null;
  private IDmeRegisterAffiliationListener registerAffiliationListener = null;
  private IDmeRegisterConnectionListener registerConnectionListener = null;

  private static final IDmeWebVerify INSTANCE = new IDmeWebVerify();

  private IDmePageFinishedListener pageFinishedListener;
  private final IDmeAccessTokenManagerListener accessTokenManagerListener = new IDmeAccessTokenManagerListener() {
    @Override
    public void onSuccess(AuthToken authToken) {
      saveAccessToken(authToken);
      if (loginGetAccessTokenListener != null) {
        loginGetAccessTokenListener.onSuccess(authToken.getAccessToken());
      }
      loginGetAccessTokenListener = null;
    }

    @Override
    public void onError(Throwable throwable) {
      if (loginGetAccessTokenListener != null) {
        loginGetAccessTokenListener.onError(throwable);
      }
      loginGetAccessTokenListener = null;
    }
  };

  /**
   * This method needs to be called before IDmeWebVerify can be used.
   * Typically it will be called from your Application class's onCreate method.
   *
   * @param context     Application context
   * @param clientId    Application client id
   * @param secretId    Application secret id
   * @param redirectUri Application redirect uri
   */
  public static void initialize(Context context, String clientId, String secretId, String redirectUri) {
    if (initialized) {
      throw new IllegalStateException("IDmeWebVerify is already initialized");
    }
    if (clientId == null) {
      throw new IllegalStateException("ClientId cannot be null");
    }
    if (redirectUri == null) {
      throw new IllegalStateException("RedirectURI cannot be null");
    }
    if (secretId == null) {
      throw new IllegalStateException("SecretId cannot be null");
    }
    idMeWebVerifyGetAuthUri = context.getString(R.string.idme_web_verify_get_auth_code_uri);
    idMeWebVerifyGetAccessTokenUri = context.getString(R.string.idme_web_verify_get_access_token_uri);
    idMeWebVerifyGetUserProfile = context.getString(R.string.idme_web_verify_get_profile_uri);
    initialized = true;
    accessTokenManager = new AccessTokenManager(context);
    refreshAccessTokenHandler = new RefreshAccessTokenHandler(accessTokenManager);
    IDmeWebVerify.clientId = clientId;
    IDmeWebVerify.redirectUri = redirectUri;
    IDmeWebVerify.secretId = secretId;
  }

  private IDmeWebVerify() {

  }

  public static IDmeWebVerify getInstance() {
    return INSTANCE;
  }

  /**
   * Checks if the application is already initialized
   *
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
  public void login(@NonNull Activity activity, @NonNull IDmeScope scope, @NonNull IDmeGetAccessTokenListener listener) {
    login(activity, scope, null, listener);
  }

  /**
   * Starts the login process.
   * This function should be used if it is known if the user wants to sign in or sign up.
   *
   * @param activity  which will be used to start the login activity
   * @param scope     The type of group verification.
   * @param loginType The type of login. The default value is {@code LoginType.SIGN_IN}
   * @param listener  The listener that will be called when the login process is finished.
   */
  public void login(@NonNull Activity activity, @NonNull IDmeScope scope, @Nullable LoginType loginType,
                    @NonNull IDmeGetAccessTokenListener listener) {
    checkInitialization();
    checkPendingRequest();

    loginGetAccessTokenListener = listener;
    if (loginType == null) {
      loginType = LoginType.SIGN_IN;
    }
    String url = createURL(scope, loginType);

    Intent intent = new Intent(activity, LoginActivity.class)
        .putExtra(WebViewActivity.EXTRA_URL, url)
        .putExtra(WebViewActivity.EXTRA_SCOPE_ID, scope.getScopeId());
    activity.startActivity(intent);
  }

  /**
   * Returns an access token
   *
   * @param scope    The type of group verification.
   * @param listener The listener that will be called when the get access token process finished.
   */
  @SuppressWarnings("unused")
  public void getAccessToken(@NonNull IDmeScope scope, @NonNull final IDmeGetAccessTokenListener listener) {
    getAccessToken(scope, false, listener);
  }

  /**
   * Returns an access token
   *
   * @param scope       The type of group verification.
   * @param forceReload Force to reload the access token.
   * @param listener    The listener that will be called when the get access token process finished.
   */
  @SuppressWarnings("unused")
  public void getAccessToken(@NonNull IDmeScope scope, boolean forceReload,
                             @NonNull IDmeGetAccessTokenListener listener) {
    checkInitialization();
    AuthToken token = accessTokenManager.getToken(scope);
    if (token == null) {
      listener.onError(new UnauthenticatedException());
    } else if (token.isValidAccessToken() && !forceReload) {
      listener.onSuccess(token.getAccessToken());
    } else if (token.isValidRefreshToken()) {
      refreshAccessTokenHandler.refreshAccessToken(scope, token, listener);
    } else {
      listener.onError(new UnauthenticatedException());
    }
  }

  /**
   * Returns the user profile who is associated with that scope
   *
   * @param scope    The type of group verification.
   * @param listener The listener that will be called when the get user profile process finished.
   */
  public void getUserProfile(@NonNull IDmeScope scope, @NonNull IDmeGetProfileListener listener) {
    AuthToken token = accessTokenManager.getToken(scope);
    if (token == null) {
      String message = String.format(Locale.US, "There is not an access token related to the %s scope", scope);
      listener.onError(new IllegalStateException(message));
    } else if (token.isValidAccessToken()) {
      String requestUrl = createRequestUrl(token.getAccessToken());
      new GetProfileConnectionTask(listener).execute(requestUrl);
    } else {
      listener.onError(new IllegalStateException("The access token is expired"));
    }
  }

  /** Deletes all session information */
  @SuppressWarnings("unused")
  public void logOut() {
    accessTokenManager.deleteSession();
  }

  /**
   * Deletes all session information regarding to the given scope
   *
   * @param scope The type of group verification.
   */
  @SuppressWarnings("unused")
  public void logOut(@NonNull IDmeScope scope) {
    accessTokenManager.deleteToken(scope);
  }

  /**
   * Starts the process of adding a new affiliation type
   *
   * @param activity        Which will be used to start the login activity.
   * @param scope           The type of group verification.
   * @param affiliationType The affiliation that will be registered.
   * @param listener        The listener that will be called when the registration process finished.
   */
  public void registerAffiliation(@NonNull Activity activity,
                                  @NonNull IDmeScope scope,
                                  IDmeAffiliationType affiliationType,
                                  @NonNull IDmeRegisterAffiliationListener listener) {
    checkInitialization();
    checkPendingRequest();

    /**
     * FIXME: use the given scope to get a local token and include it in the request when the backend support this option.
     * The registration process can proceed even if there is no token for the given scope. In such case the user must
     * sign in when the web view is opened.
     */

    pageFinishedListener = new RegisterAffiliationFinishedListener(listener, redirectUri);
    registerAffiliationListener = listener;
    String requestUrl = createRegisterAffiliationUrl(affiliationType);
    Intent intent = new Intent(activity, WebViewActivity.class)
        .putExtra(WebViewActivity.EXTRA_URL, requestUrl)
        .putExtra(WebViewActivity.EXTRA_SCOPE_ID, scope.getScopeId());
    activity.startActivity(intent);
  }

  /**
   * Starts the process of adding a new connection type
   *
   * @param activity       Which will be used to start the login activity.
   * @param scope          The type of group verification.
   * @param connectionType The connection that will be registered.
   * @param listener       The listener that will be called when the registration process finished.
   */
  public void registerConnection(Activity activity, IDmeScope scope, IDmeConnectionType connectionType,
                                 IDmeRegisterConnectionListener listener) {
    checkInitialization();
    checkPendingRequest();

    /**
     * FIXME: use the given scope to get a local token and include it in the request when the backend support this option.
     * The registration process can proceed even if there is no token for the given scope. In such case the user must
     * sign in when the web view is opened.
     */

    pageFinishedListener = new RegisterConnectionFinishedListener(listener, redirectUri);
    registerConnectionListener = listener;
    String requestUrl = createRegisterConnectionUrl(connectionType, scope);
    Intent intent = new Intent(activity, WebViewActivity.class)
        .putExtra(WebViewActivity.EXTRA_URL, requestUrl)
        .putExtra(WebViewActivity.EXTRA_SCOPE_ID, scope.getScopeId());
    activity.startActivity(intent);
  }

  IDmePageFinishedListener getPageFinishedListener() {
    return pageFinishedListener;
  }

  /**
   * Persists the access token
   *
   * @param token The auth token for the given scope.
   */
  void saveAccessToken(AuthToken token) {
    accessTokenManager.addToken(token);
  }

  /**
   * Notifies to any of the available listener about the given error
   */
  void notifyFailure(Throwable throwable) {
    if (loginGetAccessTokenListener != null) {
      loginGetAccessTokenListener.onError(throwable);
    } else if (registerAffiliationListener != null) {
      registerAffiliationListener.onError(throwable);
    } else if (registerConnectionListener != null) {
      registerConnectionListener.onError(throwable);
    }
  }

  /**
   * Removes the signIn listener
   */
  void clearSignInListener() {
    pageFinishedListener = null;
    loginGetAccessTokenListener = null;
    registerAffiliationListener = null;
    registerConnectionListener = null;
  }

  /**
   * Creates the url to be loaded in the webView
   *
   * @return URl with redirect uri, client id and scope
   */
  private String createURL(@NonNull IDmeScope scope, @NonNull LoginType loginType) {
    return idMeWebVerifyGetAuthUri
        .replace(CLIENT_ID_KEY, clientId)
        .replace(REDIRECT_URI_KEY, redirectUri)
        .replace(RESPONSE_TYPE_KEY, "code")
        .replace(SCOPE_TYPE_KEY, scope.getScopeId())
        .replace(SIGN_TYPE_KEY, loginType.getId());
  }

  /**
   * Creates the URL for the Web Request
   *
   * @return URL with proper formatted request
   */
  private String createRequestUrl(String accessToken) {
    return idMeWebVerifyGetUserProfile
        .replace(USER_TOKEN_KEY, accessToken);
  }

  /**
   * Creates the URL for adding a new affiliation type
   *
   * @param affiliationType The affiliation type that should be registered
   * @return URL with proper formatted request
   */
  private String createRegisterAffiliationUrl(IDmeAffiliationType affiliationType) {
    return idMeWebVerifyGetAuthUri
        .replace(CLIENT_ID_KEY, clientId)
        .replace(REDIRECT_URI_KEY, redirectUri)
        .replace(RESPONSE_TYPE_KEY, "code")
        .replace(SCOPE_TYPE_KEY, affiliationType.getKey());
  }

  /**
   * Creates the URL for adding a new connection type
   *
   * @param connectionType the connection that should be added
   * @param scope          The type of group verification.
   * @return URL: with proper formatted request
   */
  private String createRegisterConnectionUrl(IDmeConnectionType connectionType, IDmeScope scope) {
    String url = idMeWebVerifyGetAuthUri
        .replace(CLIENT_ID_KEY, clientId)
        .replace(REDIRECT_URI_KEY, redirectUri)
        .replace(RESPONSE_TYPE_KEY, "code")
        .replace(SCOPE_TYPE_KEY, scope.getScopeId());
    return String.format("%s&connect=%s", url, connectionType.getKey());
  }

  private void checkPendingRequest() {
    if (loginGetAccessTokenListener != null || registerAffiliationListener != null) {
      throw new IllegalStateException("The activity is already initialized");
    }
  }

  IDmeAccessTokenManagerListener getAccessTokenManagerListener() {
    return accessTokenManagerListener;
  }

  static String getIdMeWebVerifyGetAccessTokenUri() {
    return idMeWebVerifyGetAccessTokenUri;
  }

  static String getRedirectUri() {
    return redirectUri;
  }

  static String getAccessTokenQuery(@NonNull String code) {
    return new Uri.Builder()
        .appendQueryParameter("code", code)
        .appendQueryParameter("grant_type", "authorization_code")
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("client_secret", secretId)
        .build()
        .getEncodedQuery();
  }

  static String getAccessTokenFromRefreshTokenQuery(@NonNull String refreshToken) {
    return new Uri.Builder()
        .appendQueryParameter("refresh_token", refreshToken)
        .appendQueryParameter("grant_type", "refresh_token")
        .appendQueryParameter("client_id", clientId)
        .appendQueryParameter("redirect_uri", redirectUri)
        .appendQueryParameter("client_secret", secretId)
        .build()
        .getEncodedQuery();
  }
}
