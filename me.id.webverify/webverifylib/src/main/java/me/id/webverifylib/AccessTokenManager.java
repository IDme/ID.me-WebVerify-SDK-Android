package me.id.webverifylib;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mirland on 13/12/16.
 */
public class AccessTokenManager {
  private SharedPreferences preferences;
  private final AsyncSharedPreferenceLoader preferenceLoader;
  private final Map<IDmeScope, AuthToken> tokens = new HashMap<>();

  private void waitForLoad() {
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
    waitForLoad();
    return tokens.get(scope);
  }

  void addToken(IDmeScope scope, AuthToken token) {
    waitForLoad();
    tokens.put(scope, token);
    SharedPreferences.Editor edit = preferences.edit();
    edit.putString(scope.name(), ObjectHelper.toStringByteArray(token));
    edit.apply();
  }
}
