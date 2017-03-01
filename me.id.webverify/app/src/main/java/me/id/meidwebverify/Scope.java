package me.id.meidwebverify;

import android.support.annotation.NonNull;

import me.id.webverifylib.listener.IDmeScope;

/**
 * Created by remer on 6/2/17.
 */
enum Scope implements IDmeScope {
  DEFAULT("<YOUR_APP_SCOPE>"),
  ;

  private final String scopeId;

  Scope(String scopeId) {
    this.scopeId = scopeId;
  }

  @NonNull
  @Override
  public String getScopeId() {
    return scopeId;
  }
}
