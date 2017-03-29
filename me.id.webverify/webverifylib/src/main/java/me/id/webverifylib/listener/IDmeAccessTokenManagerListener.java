package me.id.webverifylib.listener;

import me.id.webverifylib.AuthToken;

/**
 * Created by mirland on 23/02/17.
 */
public interface IDmeAccessTokenManagerListener {
  void onSuccess(AuthToken authToken);
  void onError(Throwable throwable);
}
