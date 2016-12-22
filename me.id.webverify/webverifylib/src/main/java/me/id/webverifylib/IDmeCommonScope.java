package me.id.webverifylib;

import android.support.annotation.NonNull;

/**
 * The type of group verification.
 */
public enum IDmeCommonScope implements IDmeScope {
  MILITARY("military"),
  STUDENT("student"),
  TEACHER("teacher"),
  GOVERNMENT("government"),
  FIRST_RESPONDER("responder"),
  WALLET("wallet"),
  ;

  private final String id;

  IDmeCommonScope(String id) {
    this.id = id;
  }

  @NonNull
  @Override
  public String getScopeId() {
    return id;
  }
}
