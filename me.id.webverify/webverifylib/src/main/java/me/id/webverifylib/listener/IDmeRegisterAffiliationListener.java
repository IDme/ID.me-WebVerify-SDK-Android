package me.id.webverifylib.listener;

/**
 * Listener to register a new affiliation
 */
public interface IDmeRegisterAffiliationListener {
  void onSuccess();
  void onError(Throwable throwable);
}
