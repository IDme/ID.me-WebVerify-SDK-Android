package me.id.webverifylib.listener;

/**
 * Created by remer on 6/2/17.
 */
public interface IDmeRegisterConnectionListener {
  void onSuccess();
  void onError(Throwable throwable);
}