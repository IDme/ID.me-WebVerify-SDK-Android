package me.id.webverifylib;

/**
 * Created by mirland on 13/12/16.
 * Listener to get the user profile information
 */
public interface IDmeGetProfileListener {
  void onSuccess(IDmeProfile profile);
  void onError(Throwable throwable);
}
