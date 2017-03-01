package me.id.webverifylib.listener;

import android.support.annotation.NonNull;

/**
 * Used for finishing the affiliation registration flow
 */
public final class RegisterAffiliationFinishedListener extends PageFinishedListener {
  @NonNull
  private final IDmeRegisterAffiliationListener registerConnectionListener;

  public RegisterAffiliationFinishedListener(@NonNull IDmeRegisterAffiliationListener registerConnectionListener,
                                             String redirectUrl) {
    super(redirectUrl);
    this.registerConnectionListener = registerConnectionListener;
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {
    super.onCallbackResponse(responseUrl, scope);
    String error = errorFromResponseUrl(responseUrl);
    if (error == null) {
      registerConnectionListener.onSuccess();
    } else {
      registerConnectionListener.onError(new RuntimeException(error));
    }
  }
}
