package me.id.webverifylib;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * The type of group verification.
 */
public enum IDmeScope {
  MILITARY(R.string.scope_military),
  STUDENT(R.string.scope_student),
  TEACHER(R.string.scope_teacher),
  GOVERNMENT(R.string.scope_government),
  FIRST_RESPONDER(R.string.scope_first_responder),
  WALLET(R.string.scope_wallet),
  ;

  @StringRes
  private final int key;

  IDmeScope(int key) {
    this.key = key;
  }

  @StringRes
  public int getKeyRes() {
    return key;
  }

  @Nullable
  public static IDmeScope fromName(String scopeName) {
    if (scopeName == null) {
      return null;
    }
    for (IDmeScope scope : values()) {
      if (ObjectHelper.equals(scope.toString(), scopeName)) {
        return scope;
      }
    }
    return null;
  }
}
