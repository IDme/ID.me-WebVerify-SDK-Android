package me.id.webverifylib;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

final class AccessTokenManager {
  private SharedPreferences preferences;
  private final AsyncSharedPreferenceLoader preferenceLoader;
  private final Map<IDmeScope, AuthToken> tokens = new HashMap<>();

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
        IDmeScope scope = IDmeScope.fromName(entry.getKey());
        if (scope == null) {
          continue;
        }
        AuthToken authToken = ObjectHelper.fromStringByteArray(String.valueOf(entry.getValue()));
        if (authToken != null) {
          tokens.put(scope, authToken);
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
    return tokens.get(scope);
  }

  void addToken(IDmeScope scope, AuthToken token) {
    waitForTokenLoad();
    tokens.put(scope, token);
    preferences.edit()
        .putString(scope.name(), ObjectHelper.toStringByteArray(token))
        .apply();
  }

  void deleteToken(IDmeScope scope) {
    waitForTokenLoad();
    preferences.edit()
        .remove(scope.name())
        .apply();
  }

  void deleteSession() {
    waitForTokenLoad();
    preferences.edit()
        .clear()
        .apply();
  }
}