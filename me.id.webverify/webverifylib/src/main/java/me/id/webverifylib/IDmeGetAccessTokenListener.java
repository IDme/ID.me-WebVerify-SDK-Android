package me.id.webverifylib;

/**
 * Created by mirland on 13/12/16.
 * Listener to get the access token
 */
public interface IDmeGetAccessTokenListener {
  void onSuccess(String accessToken);
  void onError(Throwable throwable);
}
