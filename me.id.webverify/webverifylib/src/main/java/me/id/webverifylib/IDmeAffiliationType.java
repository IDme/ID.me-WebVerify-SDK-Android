package me.id.webverifylib;

/**
 * The type of supported affiliations
 */
public enum IDmeAffiliationType implements IDmeAffiliation {
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
