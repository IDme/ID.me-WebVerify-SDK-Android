package me.id.webverifylib;

import me.id.webverifylib.listener.IDmeScope;

/**
 * Used to handle the current app state
 */
enum State {
  LOGIN,
  REGISTER_AFFILIATION,
  REGISTER_CONNECTION,
  ;

  private IDmeScope scope;

  public IDmeScope getScope() {
    return scope;
  }

  public void setScope(IDmeScope scope) {
    this.scope = scope;
  }
}
