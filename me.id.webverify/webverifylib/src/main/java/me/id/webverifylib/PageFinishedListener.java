package me.id.webverifylib;

import android.net.Uri;
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

  public AuthToken getToken() {
    return token;
  }

  public IDmeWebVerify getWebVerify() {
    return webVerify;
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
    if (hasAccessToken(responseUrl)) {
      token = extractAccessToken(responseUrl);
      getWebVerify().saveAccessToken(scope, token);
    }
  }


  private boolean hasAccessToken(String url) {
    return url.contains(ACCESS_TOKEN_KEY);
  }

  /**
   * Extracts Access Token from URL
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
      Integer expirationTimeInSeconds = Integer.valueOf(uri.getQueryParameter(EXPIRE_TOKEN_KEY));
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.SECOND, expirationTimeInSeconds);
      authToken.setExpiration(calendar);
    } catch (NumberFormatException ex) {
      ex.printStackTrace();
    }
    return authToken;
  }
}