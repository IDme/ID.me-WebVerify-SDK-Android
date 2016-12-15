package me.id.webverifylib;

import java.io.Serializable;
import java.util.Calendar;

final class AuthToken implements Serializable {
  private IDmeScope scope;
  private String accessToken;
  private String refreshToken;
  private Calendar expiration;

  public IDmeScope getScope() {
    return scope;
  }

  public void setScope(IDmeScope scope) {
    this.scope = scope;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public Calendar getExpiration() {
    return expiration;
  }

  public void setExpiration(Calendar expiration) {
    this.expiration = expiration;
  }

  public boolean isValidToken() {
    return accessToken != null && expiration != null && Calendar.getInstance().before(expiration);
  }
}
