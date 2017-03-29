package me.id.webverifylib.listener;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import me.id.webverifylib.exception.IDmeException;
import me.id.webverifylib.exception.UserCanceledException;
import me.id.webverifylib.helper.ObjectHelper;

/**
 * Created by remer on 3/2/17.
 */
abstract class PageFinishedListener implements IDmePageFinishedListener {
  private final static String ERROR_TYPE_KEY = "error";
  private final static String ERROR_DESCRIPTION_KEY = "error_description";
  private final static String CANCELLATION_ERROR = "access_denied";

  private final String redirectUri;

  PageFinishedListener(String redirectUrl) {
    this.redirectUri = redirectUrl;
  }

  @NonNull
  Throwable getErrorFromResponseUrl(@Nullable String url) {
    if (url == null || url.isEmpty()) {
      return new IDmeException("Url cannot be null");
    }

    Uri uri = Uri.parse(fixUrl(url));
    String errorMessage = uri.getQueryParameter(ERROR_DESCRIPTION_KEY);
    String errorType = uri.getQueryParameter(ERROR_TYPE_KEY);

    if (ObjectHelper.equals(errorType, CANCELLATION_ERROR)) {
      return new UserCanceledException(errorType);
    } else {
      return new IDmeException(errorMessage == null
          ? "Failed to parse server response. Invalid format received."
          : errorMessage
      );
    }
  }

  boolean hasError(@Nullable String url) {
    if (url == null || url.isEmpty()) {
      return false;
    }

    Uri uri = Uri.parse(fixUrl(url));
    String errorMessage = uri.getQueryParameter(ERROR_DESCRIPTION_KEY);
    String errorType = uri.getQueryParameter(ERROR_TYPE_KEY);
    return (errorType != null && !errorType.isEmpty())
        || (errorMessage != null && !errorMessage.isEmpty());
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
        return fixedUrlString.equals(redirectUri);
      } else {
        return fixedUrlString.replace(Uri.parse(fixedUrlString).getQuery(), "").replace("?", "").equals(redirectUri);
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
