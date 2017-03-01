package me.id.webverifylib;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import me.id.webverifylib.helper.ObjectHelper;
import me.id.webverifylib.listener.IDmeScope;

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

  void addToken(AuthToken token) {
    waitForTokenLoad();
    String scopeId = token.getScopeId();
    tokens.put(scopeId, token);
    preferences.edit()
        .putString(scopeId, ObjectHelper.toStringByteArray(token))
        .apply();
  }

  void deleteToken(IDmeScope scope) {
    waitForTokenLoad();
    tokens.remove(scope.getScopeId());
    preferences.edit()
        .remove(scope.getScopeId())
        .apply();
  }

  void deleteSession() {
    waitForTokenLoad();
    tokens.clear();
    preferences.edit()
        .clear()
        .apply();
  }
}
