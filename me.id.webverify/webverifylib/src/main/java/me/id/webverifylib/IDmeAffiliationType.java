package me.id.webverifylib;

import android.support.annotation.StringRes;

/**
 * *
 */
public enum IDmeAffiliationType {
  GOVERNMENT("government"),
  MILITARY("military"),
  RESPONDER("responder"),
  STUDENT("student"),
  TEACHER("teacher"),
  ;

  private final String key;

  IDmeAffiliationType(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
