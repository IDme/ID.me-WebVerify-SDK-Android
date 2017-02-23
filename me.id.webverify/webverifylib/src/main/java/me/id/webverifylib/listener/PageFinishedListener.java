package me.id.webverifylib.listener;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import me.id.webverifylib.AuthToken;

/**
 * Created by remer on 3/2/17.
 */
public abstract class PageFinishedListener implements IDmePageFinishedListener {
  private final String redirectURI;

  private AuthToken token;

  public PageFinishedListener(String redirectUrl) {
    this.redirectURI = redirectUrl;
  }

  String getRedirectUrl() {
    return redirectURI;
  }

  AuthToken getToken() {
    return token;
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
      Uri url = Uri.parse(fixedUrlString);
      String query = url.getQuery();
      if (query == null || query.isEmpty()) {
        return fixedUrlString.equals(redirectURI);
      } else {
        return fixedUrlString.replace(Uri.parse(fixedUrlString).getQuery(), "").replace("?", "").equals(redirectURI);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {

  }

  @NonNull
  private String fixUrl(@NonNull String url) {
    // the service url does not respect the rfc3986, the query parameters start with "#" and it should start with "?"
    // https://tools.ietf.org/html/rfc3986
    return url.replace("#", "?");
  }
}