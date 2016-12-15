package me.id.webverifylib;

/**
 * Listener to get the access token
 */
public interface IDmeGetAccessTokenListener {
  void onSuccess(String accessToken);
  void onError(Throwable throwable);
}
