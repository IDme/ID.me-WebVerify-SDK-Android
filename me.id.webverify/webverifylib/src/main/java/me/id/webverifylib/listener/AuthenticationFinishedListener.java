package me.id.webverifylib.listener;

import android.net.Uri;

import me.id.webverifylib.helper.ObjectHelper;

/**
 * Created by remer on 3/2/17.
 */
public final class AuthenticationFinishedListener extends PageFinishedListener {
  static final String CODE_QUERY_PARAMETER = "code";

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
      if (null != authCode) {
        iDmeGetAuthCodeListener.onSuccess(authCode);
      } else {
        String error = errorFromResponseUrl(responseUrl);
        iDmeGetAuthCodeListener.onError(new IllegalStateException(error == null
            ? "Failed to parse server response. Invalid format received."
            : error
        ));
      }
    }
  }
}
