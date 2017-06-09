package me.id.webverifylib.listener;

/**
 * A completable listener
 */
public interface IDmeCompletableListener {
  void onSuccess();
  void onError(Throwable throwable);
}
