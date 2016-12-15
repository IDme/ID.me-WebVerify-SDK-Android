package me.id.webverifylib;

/**
 * Listener to get the user profile information
 */
public interface IDmeGetProfileListener {
  void onSuccess(IDmeProfile profile);
  void onError(Throwable throwable);
}
