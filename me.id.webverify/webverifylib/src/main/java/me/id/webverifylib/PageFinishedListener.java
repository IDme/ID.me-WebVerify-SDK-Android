package me.id.webverifylib;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by remer on 3/2/17.
 */
abstract class PageFinishedListener implements IDmePageFinishedListener {
  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String EXPIRE_TOKEN_KEY = "expires_in";

  private final String redirectURI;
  private final IDmeWebVerify webVerify;

  private AuthToken token;

  PageFinishedListener(IDmeWebVerify webVerify, String redirectUrl) {
    this.webVerify = webVerify;
    this.redirectURI = redirectUrl;
  }

  String getRedirectUrl() {
    return redirectURI;
  }

  AuthToken getToken() {
    return token;
  }

  IDmeWebVerify getWebVerify() {
    return webVerify;
  }

  @Nullable
  String errorFromResponseUrl(@Nullable String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    Uri uri = Uri.parse(fixUrl(url));
    return uri.getQueryParameter("error_description");
  }

  @Override
  public boolean isCallbackUrl(String urlString) {
    // the service url does not respect the rfc3986, the query parameters start with "#" and it should start with "?"
    // https://tools.ietf.org/html/rfc3986
    String fixedUrlString = urlString.replace("#", "?");
    Log.d("SDK", fixedUrlString);

    try {
      URL url = new URL(fixedUrlString);
      String query = url.getQuery();
      if (query == null || query.isEmpty()) {
        return fixedUrlString.equals(redirectURI);
      } else {
        return fixedUrlString.replace("?", "").replace(url.getQuery(), "").equals(redirectURI);
      }
    } catch (MalformedURLException ex) {
      ex.printStackTrace();
      return false;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {
    token = extractAccessToken(responseUrl);
    if (token != null) {
      getWebVerify().saveAccessToken(scope, token);
    }
  }

  /**
   * Extracts Access Token from URL
   *
   * @param url URL that contains access token
   */
  @Nullable
  private AuthToken extractAccessToken(@NonNull String url) {
    if (!url.contains(ACCESS_TOKEN_KEY)) {
      return null;
    }

    Uri uri = Uri.parse(fixUrl(url));
    AuthToken authToken = new AuthToken();
    authToken.setAccessToken(uri.getQueryParameter(ACCESS_TOKEN_KEY));
    try {
      Integer expirationTimeInSeconds = Integer.valueOf(uri.getQueryParameter(EXPIRE_TOKEN_KEY));
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.SECOND, expirationTimeInSeconds);
      authToken.setExpiration(calendar);
    } catch (NumberFormatException ex) {
      ex.printStackTrace();
    }
    return authToken;
  }

  @NonNull
  private String fixUrl(@NonNull String url) {
    // the service url does not respect the rfc3986, the query parameters start with "#" and it should start with "?"
    // https://tools.ietf.org/html/rfc3986
    return url.replace("#", "?");
  }
}