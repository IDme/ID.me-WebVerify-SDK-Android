package me.id.webverifylib;

/**
 * Created by remer on 6/2/17.
 */
public enum IDmeConnectionType {
  DS_LOGON("dslogon"),
  FACEBOOK("facebook"),
  GOOGLE_PLUS("google"),
  LINEDIN("linkedin"),
  PAYPAL("paypal"),
  ;

  private final String key;

  IDmeConnectionType(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
