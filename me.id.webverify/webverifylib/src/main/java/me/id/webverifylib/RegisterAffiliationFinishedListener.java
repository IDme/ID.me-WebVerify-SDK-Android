package me.id.webverifylib;

/**
 * Created by remer on 3/2/17.
 */
final class RegisterAffiliationFinishedListener extends PageFinishedListener {
  RegisterAffiliationFinishedListener(IDmeWebVerify webVerify, String redirectUrl) {
    super(webVerify, redirectUrl);
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {
    super.onCallbackResponse(responseUrl, scope);
    getWebVerify().notifyAffiliationRegistration();
  }
}
