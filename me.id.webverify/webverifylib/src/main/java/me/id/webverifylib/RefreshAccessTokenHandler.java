package me.id.webverifylib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.id.webverifylib.listener.IDmeAccessTokenManagerListener;
import me.id.webverifylib.listener.IDmeGetAccessTokenListener;
import me.id.webverifylib.listener.IDmeScope;
import me.id.webverifylib.networking.GetAccessTokenConnectionTask;

/**
 * Created by mirland on 23/02/17.
 */
final class RefreshAccessTokenHandler {
  private static final Map<String, List<IDmeGetAccessTokenListener>> LISTENERS = new HashMap<>();
  private static final Map<String, IDmeAccessTokenManagerListener> SCOPE_LISTENERS = new HashMap<>();

  static void refreshAccessToken(IDmeScope scope, AuthToken authToken, IDmeGetAccessTokenListener listener) {
    final String scopeId = authToken.getScopeId();
    synchronized (LISTENERS) {
      List<IDmeGetAccessTokenListener> scopeListeners = LISTENERS.get(scopeId);
      if (scopeListeners == null) {
        scopeListeners = new ArrayList<>();
        LISTENERS.put(scopeId, scopeListeners);
      }
      if (scopeListeners.size() == 0) {
        refreshToken(scope, authToken.getRefreshToken());
      }
      scopeListeners.add(listener);
    }
  }

  private static synchronized IDmeAccessTokenManagerListener getScopeListener(IDmeScope scope) {
    final String scopeId = scope.getScopeId();
    IDmeAccessTokenManagerListener scopeListener = SCOPE_LISTENERS.get(scopeId);
    if (scopeListener == null) {
      scopeListener = new IDmeAccessTokenManagerListener() {
        @Override
        public void onSuccess(AuthToken authToken) {
          synchronized (LISTENERS) {
            IDmeWebVerify.getInstance().saveAccessToken(authToken);
            List<IDmeGetAccessTokenListener> scopeListeners = LISTENERS.get(scopeId);
            for (IDmeGetAccessTokenListener listener : scopeListeners) {
              listener.onSuccess(authToken.getAccessToken());
            }
            scopeListeners.clear();
          }
        }

        @Override
        public void onError(Throwable throwable) {
          synchronized (LISTENERS) {
            List<IDmeGetAccessTokenListener> scopeListeners = LISTENERS.get(scopeId);
            for (IDmeGetAccessTokenListener listener : scopeListeners) {
              listener.onError(throwable);
            }
            scopeListeners.clear();
          }
        }
      };
      SCOPE_LISTENERS.put(scopeId, scopeListener);
    }
    return scopeListener;
  }

  private static void refreshToken(IDmeScope scope, String refreshToken) {
    IDmeAccessTokenManagerListener listener = getScopeListener(scope);
    new GetAccessTokenConnectionTask(IDmeWebVerify.getAccessTokenFromRefreshTokenQuery(refreshToken), listener, scope)
        .execute(IDmeWebVerify.getIdMeWebVerifyGetAccessTokenURI());
  }
}
