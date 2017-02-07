package me.id.webverifylib;

/**
 * Created by remer on 6/2/17.
 */
class RegisterConnectionFinishedListener extends PageFinishedListener {
  RegisterConnectionFinishedListener(IDmeWebVerify webVerify, String redirectUrl) {
    super(webVerify, redirectUrl);
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {
    super.onCallbackResponse(responseUrl, scope);
    String error = errorFromResponseUrl(responseUrl);
    if (error == null) {
      getWebVerify().notifyConnectionRegistration();
    } else {
      getWebVerify().notifyFailure(new RuntimeException(error));
    }
  }
}
