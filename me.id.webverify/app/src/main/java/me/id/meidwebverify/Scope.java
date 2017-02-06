package me.id.meidwebverify;

import android.support.annotation.NonNull;

import me.id.webverifylib.IDmeScope;

/**
 * Created by remer on 6/2/17.
 */

class Scope implements IDmeScope {
  private static final Scope instance = new Scope("<your_app_scope>");

  private final String scopeId;

  private Scope(String scopeId) {
    this.scopeId = scopeId;
  }

  static Scope getInstance() {
    return instance;
  }

  @NonNull
  @Override
  public String getScopeId() {
    return scopeId;
  }
}
