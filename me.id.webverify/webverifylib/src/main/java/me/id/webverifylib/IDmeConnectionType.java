package me.id.webverifylib;

import androidx.annotation.NonNull;

/**
 * The type of supported connections
 */
public enum IDmeConnectionType implements IDmeConnection {
  FACEBOOK("facebook"),
  GOOGLE_PLUS("google"),
  LINEDIN("linkedin"),
  PAYPAL("paypal"),
  ;

  private final String key;

  IDmeConnectionType(String key) {
    this.key = key;
  }

  @NonNull
  @Override
  public String getKey() {
    return key;
  }
}
