package me.id.webverifylib.listener;

import android.support.annotation.NonNull;

/**
 * Created by remer on 6/2/17.
 */
public class RegisterConnectionFinishedListener extends PageFinishedListener {
  @NonNull
  private final IDmeRegisterConnectionListener listener;

  public RegisterConnectionFinishedListener(@NonNull IDmeRegisterConnectionListener listener, String redirectUrl) {
    super(redirectUrl);
    this.listener = listener;
  }

  @Override
  public void onCallbackResponse(String responseUrl, IDmeScope scope) {
    super.onCallbackResponse(responseUrl, scope);
    String error = errorFromResponseUrl(responseUrl);
    if (error == null) {
      listener.onSuccess();
    } else {
      listener.onError(new RuntimeException(error));
    }
  }
}
