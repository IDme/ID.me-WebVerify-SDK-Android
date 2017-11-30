package me.id.webverifylib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Locale;

import me.id.webverifylib.exception.IDmeException;
import me.id.webverifylib.exception.UnauthenticatedException;
import me.id.webverifylib.exception.UserCanceledException;
import me.id.webverifylib.helper.CodeVerifierUtil;
import me.id.webverifylib.helper.Preconditions;
import me.id.webverifylib.listener.IDmeCompletableListener;
import me.id.webverifylib.listener.IDmeGetAccessTokenListener;
import me.id.webverifylib.listener.IDmeGetProfileListener;
import me.id.webverifylib.listener.IDmeScope;
import me.id.webverifylib.networking.GetProfileConnectionTask;

public final class IDmeWebVerify {
  public static final String TAG = "ID.me SDK";

  private static final String PARAM_ACCESS_TOKEN = "access_token";
  private static final String PARAM_CLIENT_ID = "client_id";
  private static final String PARAM_CLIENT_SECRET = "client_secret";
  static final String PARAM_CODE = "code";
  private static final String PARAM_CODE_CHALLENGE = "code_challenge";
  private static final String PARAM_CODE_CHALLENGE_METHOD = "code_challenge_method";
  private static final String PARAM_CODE_VERIFIER = "code_verifier";
  private static final String PARAM_CONNECT_TYPE = "connect";
  private static final String PARAM_GRANT_TYPE = "grant_type";
  private static final String PARAM_REDIRECT_URI = "redirect_uri";
  private static final String PARAM_REFRESH_TOKEN = "refresh_token";
  private static final String PARAM_SCOPE_TYPE = "scope";
  private static final String PARAM_RESPONSE_TYPE = "response_type";
  private static final String PARAM_TYPE = "type";
  private static final String SIGN_TYPE_KEY = "op";
  private static final String RESPONSE_TYPE_VALUE = "code";
  private static final String LOGOUT_TYPE_VALUE = "logout";

  private static Uri idMeWebVerifyAccessTokenUri;
  private static Uri idMeWebVerifyGetCommonUri;
  private static Uri idMeWebVerifyGetLogoutUri;
  private static Uri idMeWebVerifyGetUserProfileUri;

  private static AccessTokenManager accessTokenManager;
  private static RefreshAccessTokenHandler refreshAccessTokenHandler;
  private static String clientId;
  private static String redirectUri;
  private static String clientSecret;
  private static boolean initialized;
  private static State currentState;

  private IDmePreferences preferences;

  private IDmeGetAccessTokenListener accessTokenCallback = null;
  private IDmeCompletableListener completableCallback = null;

  private static final IDmeWebVerify INSTANCE = new IDmeWebVerify();

  /**
   * This method needs to be called before IDmeWebVerify can be used.
   * Typically it will be called from your Application class's onCreate method.
   *
   * @param context      Application context
   * @param clientId     Application client id
   * @param clientSecret Application client secret
   * @param redirectUri  Application redirect uri
   * @throws IDmeException if something went wrong
   */
  public static void initialize(Context context, String clientId, String clientSecret, String redirectUri) {
    if (initialized) {
      throw new IDmeException("IDmeWebVerify is already initialized");
    }
    Preconditions.checkNotNull(clientId, "ClientId cannot be null");
    Preconditions.checkNotNull(redirectUri, "RedirectURI cannot be null");
    Preconditions.checkNotNull(clientSecret, "Client secret cannot be null");
    if (!isRedirectUriRegistered(context, redirectUri)) {
      Log.e(TAG, "redirect_uri is not handled by any activity in this app! "
          + "Ensure that the idmeAuthRedirectScheme in your build.gradle file "
          + "is correctly configured, or that an appropriate intent filter "
          + "exists in your app manifest.");
    }
    idMeWebVerifyAccessTokenUri = Uri.parse(context.getString(R.string.idme_web_verify_get_access_token_uri));
    idMeWebVerifyGetCommonUri = Uri.parse(context.getString(R.string.idme_web_verify_get_common_uri));
    idMeWebVerifyGetLogoutUri = Uri.parse(context.getString(R.string.idme_web_verify_get_logout_uri));
    idMeWebVerifyGetUserProfileUri = Uri.parse(context.getString(R.string.idme_web_verify_get_profile_uri));
    initialized = true;
    accessTokenManager = new AccessTokenManager(context);
    refreshAccessTokenHandler = new RefreshAccessTokenHandler(accessTokenManager);
    IDmeWebVerify.clientId = clientId;
    IDmeWebVerify.redirectUri = redirectUri;
    IDmeWebVerify.clientSecret = clientSecret;

    INSTANCE.preferences = new IDmePreferences(context);
    INSTANCE.preferences.storeApplicationNameFromContext(context);
  }

  private IDmeWebVerify() {}

  public static IDmeWebVerify getInstance() {
    return INSTANCE;
  }

  @NonNull
  IDmePreferences getPreferences() {
    return preferences;
  }

  /**
   * Ensure that the redirect URI declared in the configuration is handled by some activity
   * in the app, by querying the package manager speculatively
   */
  private static boolean isRedirectUriRegistered(Context context, String redirectUri) {
    Intent redirectIntent = new Intent();
    redirectIntent.setPackage(context.getPackageName());
    redirectIntent.setAction(Intent.ACTION_VIEW);
    redirectIntent.addCategory(Intent.CATEGORY_BROWSABLE);
    redirectIntent.setData(Uri.parse(redirectUri));

    return !context.getPackageManager().queryIntentActivities(redirectIntent, 0).isEmpty();
  }

  /**
   * Checks if the application is already initialized
   *
   * @throws IDmeException Throws exception if the library hasn't been initialized yet
   */
  private void checkInitialization() {
    if (!initialized) {
      throw new IDmeException("IDmeWebVerify has to be initialized before use any operation");
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
   * @throws UserCanceledException    if the user cancel the action
   * @throws UnauthenticatedException if the auth information is not valid
   * @throws IDmeException            if something went wrong
   */
  public void login(@NonNull Activity activity, @NonNull IDmeScope scope, @Nullable LoginType loginType,
                    @NonNull IDmeGetAccessTokenListener listener) {
    checkInitialization();
    setCurrentState(State.LOGIN, scope);
    accessTokenCallback = listener;
    if (loginType == null) {
      loginType = LoginType.SIGN_IN;
    }

    String requestUrl = createURL(scope, loginType);
    openCustomTabActivity(activity, requestUrl);
  }

  private void openCustomTabActivity(@NonNull Activity activity, String requestUrl) {
    Intent intent = new Intent(activity, IDmeCustomTabsActivity.class)
        .putExtra(IDmeCustomTabsActivity.EXTRA_URL, requestUrl)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    activity.startActivity(intent);
  }

  /**
   * Returns an access token
   *
   * @param scope    The type of group verification.
   * @param listener The listener that will be called when the get access token process finished.
   * @throws UnauthenticatedException if the auth information is not valid
   * @throws IDmeException            if something went wrong
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
   * @throws UnauthenticatedException if the auth information is not valid
   * @throws IDmeException            if something went wrong
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
      token.invalidateAccessToken();
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
   * @throws IDmeException if something went wrong
   */
  public void getUserProfile(@NonNull IDmeScope scope, @NonNull IDmeGetProfileListener listener) {
    AuthToken token = accessTokenManager.getToken(scope);
    if (token == null) {
      String message = String.format(Locale.US, "There is not an access token related to the %s scope", scope);
      listener.onError(new IDmeException(message));
    } else if (token.isValidAccessToken()) {
      String requestUrl = createGetProfileRequestUrl(token.getAccessToken());
      new GetProfileConnectionTask(listener).execute(requestUrl);
    } else {
      listener.onError(new IDmeException("The access token is expired"));
    }
  }

  /**
   * Deletes all session information
   *
   * @param activity Context used to start the logout activity
   * @param listener The listener that will be called when the logout process finished.
   */
  @SuppressWarnings("unused")
  public void logOut(@NonNull Activity activity, @NonNull IDmeCompletableListener listener) {
    checkInitialization();
    setCurrentState(State.LOGOUT, null);

    accessTokenManager.deleteSession();
    completableCallback = listener;

    String requestUrl = createLogoutRequestUrl(null);
    openCustomTabActivity(activity, requestUrl);
  }

  /**
   * Deletes all session information regarding to the given scope
   *
   * @param activity Context used to start the logout activity
   * @param scope    The type of group verification.
   * @param listener The listener that will be called when the logout process finished.
   */
  @SuppressWarnings("unused")
  public void logOut(@NonNull Activity activity, @NonNull IDmeScope scope, @NonNull IDmeCompletableListener listener) {
    checkInitialization();
    setCurrentState(State.LOGOUT, scope);

    accessTokenManager.deleteToken(scope);
    completableCallback = listener;

    String requestUrl = createLogoutRequestUrl(null);
    openCustomTabActivity(activity, requestUrl);
  }

  /**
   * Starts the process of adding a new affiliation type
   *
   * @param activity        Which will be used to start the login activity.
   * @param scope           The type of group verification.
   * @param affiliationType The affiliation that will be registered.
   * @param listener        The listener that will be called when the registration process finished.
   * @throws UserCanceledException    if the user cancel the action
   * @throws UnauthenticatedException if the auth information is not valid
   * @throws IDmeException            if something went wrong
   */
  public void registerAffiliation(@NonNull Activity activity,
                                  @NonNull IDmeScope scope,
                                  IDmeAffiliationType affiliationType,
                                  @NonNull IDmeCompletableListener listener) {
    checkInitialization();
    setCurrentState(State.REGISTER_AFFILIATION, scope);

    completableCallback = listener;
    String requestUrl = createRegisterAffiliationUrl(affiliationType);
    openCustomTabActivity(activity, requestUrl);
  }

  /**
   * Starts the process of adding a new connection type
   *
   * @param activity       Which will be used to start the login activity.
   * @param scope          The type of group verification.
   * @param connectionType The connection that will be registered.
   * @param listener       The listener that will be called when the registration process finished.
   * @throws UserCanceledException    if the user cancel the action
   * @throws UnauthenticatedException if the auth information is not valid
   * @throws IDmeException            if something went wrong
   */
  public void registerConnection(@NonNull Activity activity, @NonNull IDmeScope scope,
                                 @NonNull IDmeConnectionType connectionType,
                                 @NonNull IDmeCompletableListener listener) {
    checkInitialization();
    setCurrentState(State.REGISTER_CONNECTION, scope);

    completableCallback = listener;
    String requestUrl = createRegisterConnectionUrl(connectionType, scope);
    openCustomTabActivity(activity, requestUrl);
  }

  /**
   * Persists the access token
   *
   * @param token The auth token for the given scope.
   */
  void saveAccessToken(AuthToken token) {
    accessTokenManager.addToken(token);
  }

  /** Notifies the error to the appropriate listener */
  synchronized void notifyFailure(Throwable throwable) {
    if (currentState == null) {
      Log.e(TAG, "Cannot notify failure due to there is not state set.", throwable);
      return;
    }
    switch (currentState) {
      case LOGIN:
        accessTokenCallback.onError(throwable);
        break;
      case LOGOUT:
      case REGISTER_AFFILIATION:
      case REGISTER_CONNECTION:
        completableCallback.onError(throwable);
        break;
    }
    clearListenersAndClearState();
  }

  /** Notifies the success to the appropriate listener */
  void notifySuccess(AuthToken authToken) {
    saveAccessToken(authToken);
    switch (currentState) {
      case LOGIN:
        accessTokenCallback.onSuccess(authToken.getAccessToken());
        break;
      case REGISTER_AFFILIATION:
      case REGISTER_CONNECTION:
        completableCallback.onSuccess();
        break;
      default:
    }
    clearListenersAndClearState();
  }

  /** Notifies the logout success to the appropriate listener */
  void notifyLogoutSuccess() {
    switch (currentState) {
      case LOGOUT:
        completableCallback.onSuccess();
        break;
      default:
        throw new IDmeException("Current state is not recognized");
    }
    clearListenersAndClearState();
  }

  /** Removes all listeners */
  private void clearListenersAndClearState() {
    accessTokenCallback = null;
    completableCallback = null;
    currentState = null;
  }

  /**
   * Creates the url to be loaded in the webView
   *
   * @return URl with redirect uri, client id and scope
   */
  private String createURL(@NonNull IDmeScope scope, @NonNull LoginType loginType) {
    return getCommonUri()
        .appendQueryParameter(PARAM_CODE_CHALLENGE, currentState.getCodeChallenge())
        .appendQueryParameter(PARAM_CODE_CHALLENGE_METHOD, currentState.getCodeVerifierMethod())
        .appendQueryParameter(PARAM_SCOPE_TYPE, scope.getScopeId())
        .appendQueryParameter(SIGN_TYPE_KEY, loginType.getId())
        .build()
        .toString();
  }

  /**
   * Creates the URL for getting the user profile
   *
   * @return URL with proper formatted request
   */
  private String createGetProfileRequestUrl(String accessToken) {
    return new Uri.Builder()
        .scheme(idMeWebVerifyGetUserProfileUri.getScheme())
        .authority(idMeWebVerifyGetUserProfileUri.getHost())
        .path(idMeWebVerifyGetUserProfileUri.getPath())
        .appendQueryParameter(PARAM_ACCESS_TOKEN, accessToken)
        .build()
        .toString();
  }

  /**
   * Creates the URL for delete the custom tab session
   *
   * @param scope An optional scope param
   * @return URL with proper formatted request
   */
  private String createLogoutRequestUrl(@Nullable IDmeScope scope) {
    Uri.Builder urlBuilder = idMeWebVerifyGetLogoutUri
        .buildUpon()
        .appendQueryParameter(PARAM_CLIENT_ID, clientId)
        .appendQueryParameter(PARAM_REDIRECT_URI, redirectUri)
        .appendQueryParameter(PARAM_TYPE, LOGOUT_TYPE_VALUE);
    if (scope != null) {
      urlBuilder.appendQueryParameter(PARAM_SCOPE_TYPE, scope.getScopeId());
    }
    return urlBuilder
        .build()
        .toString();
  }

  /**
   * Creates the URL for adding a new affiliation type
   *
   * @param affiliationType The affiliation type that should be registered
   * @return URL with proper formatted request
   */
  private String createRegisterAffiliationUrl(IDmeAffiliationType affiliationType) {
    return getCommonUri()
        .appendQueryParameter(PARAM_CODE_CHALLENGE, currentState.getCodeChallenge())
        .appendQueryParameter(PARAM_CODE_CHALLENGE_METHOD, currentState.getCodeVerifierMethod())
        .appendQueryParameter(PARAM_SCOPE_TYPE, affiliationType.getKey())
        .build()
        .toString();
  }

  /**
   * Creates the URL for adding a new connection type
   *
   * @param connectionType the connection that should be added
   * @param scope          The type of group verification.
   * @return URL: with proper formatted request
   */
  private String createRegisterConnectionUrl(IDmeConnectionType connectionType, IDmeScope scope) {
    return getCommonUri()
        .appendQueryParameter(PARAM_CODE_CHALLENGE, currentState.getCodeChallenge())
        .appendQueryParameter(PARAM_CODE_CHALLENGE_METHOD, currentState.getCodeVerifierMethod())
        .appendQueryParameter(PARAM_CONNECT_TYPE, connectionType.getKey())
        .appendQueryParameter(PARAM_SCOPE_TYPE, scope.getScopeId())
        .build()
        .toString();
  }

  /**
   * Creates the common URL which contains the client id, the client, the redirect uri and the response type
   */
  private Uri.Builder getCommonUri() {
    return idMeWebVerifyGetCommonUri
        .buildUpon()
        .appendQueryParameter(PARAM_CLIENT_ID, clientId)
        .appendQueryParameter(PARAM_REDIRECT_URI, redirectUri)
        .appendQueryParameter(PARAM_RESPONSE_TYPE, RESPONSE_TYPE_VALUE);
  }

  private synchronized void setCurrentState(State state, IDmeScope scope) {
    if (currentState == null) {
      currentState = state;
      currentState.setScope(scope);
      currentState.setCodeVerifier(CodeVerifierUtil.generateRandomCodeVerifier());
    } else {
      throw new IDmeException("A process is already initialized");
    }
  }

  static String getIdMeWebVerifyAccessTokenUri() {
    return idMeWebVerifyAccessTokenUri.toString();
  }

  @Nullable
  static State getCurrentState() {
    return currentState;
  }

  static String getAccessTokenQuery(@NonNull String code) {
    return new Uri.Builder()
        .appendQueryParameter(PARAM_CLIENT_ID, clientId)
        .appendQueryParameter(PARAM_CLIENT_SECRET, clientSecret)
        .appendQueryParameter(PARAM_CODE, code)
        .appendQueryParameter(PARAM_CODE_VERIFIER, currentState.getCodeVerifier())
        .appendQueryParameter(PARAM_GRANT_TYPE, "authorization_code")
        .appendQueryParameter(PARAM_REDIRECT_URI, redirectUri)
        .build()
        .getEncodedQuery();
  }

  static String getAccessTokenFromRefreshTokenQuery(@NonNull String refreshToken) {
    return new Uri.Builder()
        .appendQueryParameter(PARAM_CLIENT_ID, clientId)
        .appendQueryParameter(PARAM_CLIENT_SECRET, clientSecret)
        .appendQueryParameter(PARAM_GRANT_TYPE, "refresh_token")
        .appendQueryParameter(PARAM_REDIRECT_URI, redirectUri)
        .appendQueryParameter(PARAM_REFRESH_TOKEN, refreshToken)
        .build()
        .getEncodedQuery();
  }
}
