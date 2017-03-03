package me.id.webverifylib.listener;

import android.net.Uri;

import me.id.webverifylib.helper.ObjectHelper;

/**
 * Used for finishing the authentication flow
 */
public final class AuthenticationFinishedListener extends PageFinishedListener {
  private static final String CODE_QUERY_PARAMETER = "code";

  private final IDmeGetAuthCodeListener iDmeGetAuthCodeListener;
  private final String scheme;

  public AuthenticationFinishedListener(IDmeGetAuthCodeListener iDmeGetAuthCodeListener,  String redirectUrl) {
    super(redirectUrl);
    this.iDmeGetAuthCodeListener = iDmeGetAuthCodeListener;
    scheme = Uri.parse(redirectUrl).getScheme();
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {
    super.onCallbackResponse(responseUrl, scope);
    Uri uri = Uri.parse(responseUrl);
    if (ObjectHelper.equals(uri.getScheme(), scheme)) {
      String authCode = uri.getQueryParameter(CODE_QUERY_PARAMETER);
      if (authCode == null) {
        Throwable error = getErrorFromResponseUrl(responseUrl);
        iDmeGetAuthCodeListener.onError(error);
      } else {
        iDmeGetAuthCodeListener.onSuccess(authCode);
      }
    }
  }
}
