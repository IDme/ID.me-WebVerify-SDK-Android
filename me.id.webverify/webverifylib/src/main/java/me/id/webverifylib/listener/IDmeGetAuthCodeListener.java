package me.id.webverifylib.listener;

/**
 * Listener to get the auth token
 */
public interface IDmeGetAuthCodeListener {
  void onSuccess(String authCode);
  void onError(Throwable throwable);
}
