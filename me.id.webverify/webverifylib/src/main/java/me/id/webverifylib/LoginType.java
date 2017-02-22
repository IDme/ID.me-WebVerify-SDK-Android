package me.id.webverifylib;

/**
 * Created by mirland on 20/02/17.
 */
public enum LoginType {
  SIGN_IN("signin"),
  SIGN_UP("signup"),
  ;

  private final String id;

  LoginType(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}
