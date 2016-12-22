package me.id.webverifylib;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

final class AccessTokenManager {
  private SharedPreferences preferences;
  private final AsyncSharedPreferenceLoader preferenceLoader;
  private final Map<String, AuthToken> tokens = new HashMap<>();

  private void waitForTokenLoad() {
    if (preferences == null) {
      preferences = preferenceLoader.get();
      loadTokensFromSharedPreferences();
    }
  }

  private void loadTokensFromSharedPreferences() {
    Map<String, ?> keys = preferences.getAll();
    if (keys != null) {
      for (Map.Entry<String, ?> entry : keys.entrySet()) {
        String scopeId = entry.getKey();
        if (scopeId == null) {
          continue;
        }
        AuthToken authToken = ObjectHelper.fromStringByteArray(String.valueOf(entry.getValue()));
        if (authToken != null) {
          tokens.put(scopeId, authToken);
        }
      }
    }
  }

  AccessTokenManager(Context context) {
    preferenceLoader = new AsyncSharedPreferenceLoader(context);
  }

  @Nullable
  AuthToken getToken(IDmeScope scope) {
    waitForTokenLoad();
    return tokens.get(scope.getScopeId());
  }

  void addToken(IDmeScope scope, AuthToken token) {
    waitForTokenLoad();
    tokens.put(scope.getScopeId(), token);
    preferences.edit()
        .putString(scope.getScopeId(), ObjectHelper.toStringByteArray(token))
        .apply();
  }

  void deleteToken(IDmeScope scope) {
    waitForTokenLoad();
    preferences.edit()
        .remove(scope.getScopeId())
        .apply();
  }

  void deleteSession() {
    waitForTokenLoad();
    preferences.edit()
        .clear()
        .apply();
  }
}
