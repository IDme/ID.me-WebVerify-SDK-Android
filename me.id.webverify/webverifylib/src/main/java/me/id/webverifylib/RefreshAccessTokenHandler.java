package me.id.webverifylib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import me.id.webverifylib.exception.UnauthenticatedException;
import me.id.webverifylib.listener.IDmeAccessTokenManagerListener;
import me.id.webverifylib.listener.IDmeGetAccessTokenListener;
import me.id.webverifylib.listener.IDmeScope;
import me.id.webverifylib.networking.GetAccessTokenConnectionTask;

/**
 * Created by mirland on 23/02/17.
 */
final class RefreshAccessTokenHandler {
  private final AccessTokenManager accessTokenManager;
  private final Map<String, List<IDmeGetAccessTokenListener>> listeners = new HashMap<>();
  private final Map<String, IDmeAccessTokenManagerListener> scopeListeners = new HashMap<>();
  private final Set<AuthToken> usedTokens = Collections.newSetFromMap(new WeakHashMap<AuthToken, Boolean>());

  RefreshAccessTokenHandler(AccessTokenManager accessTokenManager) {
    this.accessTokenManager = accessTokenManager;
  }

  void refreshAccessToken(IDmeScope scope, AuthToken authToken, IDmeGetAccessTokenListener listener) {
    final String scopeId = authToken.getScopeId();
    synchronized (listeners) {
      if (usedTokens.contains(authToken)) {
        AuthToken token = accessTokenManager.getToken(scope);
        if (token == null) {
          listener.onError(new UnauthenticatedException());
        } else {
          listener.onSuccess(token.getAccessToken());
        }
      }
      List<IDmeGetAccessTokenListener> scopeListeners = listeners.get(scopeId);
      if (scopeListeners == null) {
        scopeListeners = new ArrayList<>();
        listeners.put(scopeId, scopeListeners);
      }
      if (scopeListeners.size() == 0) {
        refreshToken(scope, authToken.getRefreshToken());
      }
      scopeListeners.add(listener);
    }
  }

  private synchronized IDmeAccessTokenManagerListener getScopeListener(IDmeScope scope) {
    final String scopeId = scope.getScopeId();
    IDmeAccessTokenManagerListener scopeListener = scopeListeners.get(scopeId);
    if (scopeListener == null) {
      scopeListener = new IDmeAccessTokenManagerListener() {
        @Override
        public void onSuccess(AuthToken authToken) {
          synchronized (listeners) {
            usedTokens.add(authToken);
            IDmeWebVerify.getInstance().saveAccessToken(authToken);
            List<IDmeGetAccessTokenListener> scopeListeners = listeners.get(scopeId);
            for (IDmeGetAccessTokenListener listener : scopeListeners) {
              listener.onSuccess(authToken.getAccessToken());
            }
            scopeListeners.clear();
          }
        }

        @Override
        public void onError(Throwable throwable) {
          synchronized (listeners) {
            List<IDmeGetAccessTokenListener> scopeListeners = listeners.get(scopeId);
            for (IDmeGetAccessTokenListener listener : scopeListeners) {
              listener.onError(throwable);
            }
            scopeListeners.clear();
          }
        }
      };
      scopeListeners.put(scopeId, scopeListener);
    }
    return scopeListener;
  }

  private void refreshToken(IDmeScope scope, String refreshToken) {
    IDmeAccessTokenManagerListener listener = getScopeListener(scope);
    new GetAccessTokenConnectionTask(IDmeWebVerify.getAccessTokenFromRefreshTokenQuery(refreshToken), listener, scope)
        .execute(IDmeWebVerify.getIdMeWebVerifyGetAccessTokenURI());
  }
}
