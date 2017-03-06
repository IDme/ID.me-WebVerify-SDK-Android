package me.id.webverifylib.listener;

import android.support.annotation.NonNull;

/**
 * Used for finishing the connection registration flow
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
    if (hasError(responseUrl)) {
      listener.onSuccess();
    } else {
      Throwable error = getErrorFromResponseUrl(responseUrl);
      listener.onError(error);
    }
  }
}
