package me.id.webverifylib;

/**
 * Created by remer on 3/2/17.
 */
final class AuthenticationFinishedListener extends PageFinishedListener{
  AuthenticationFinishedListener(IDmeWebVerify webVerify, String redirectUrl) {
    super(webVerify, redirectUrl);
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {
    super.onCallbackResponse(responseUrl, scope);
    if (getToken() == null) {
      String error = errorFromResponseUrl(responseUrl);
      getWebVerify().notifyFailure(new IllegalStateException(error == null ? "Failed to parse server response. Invalid format received." : error));
    } else {
      getWebVerify().notifyAccessToken(getToken());
    }
  }
}
