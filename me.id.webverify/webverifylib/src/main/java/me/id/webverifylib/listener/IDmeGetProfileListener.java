package me.id.webverifylib.listener;

import me.id.webverifylib.IDmeProfile;

/**
 * Listener to get the user profile information
 */
public interface IDmeGetProfileListener {
  void onSuccess(IDmeProfile profile);
  void onError(Throwable throwable);
}
