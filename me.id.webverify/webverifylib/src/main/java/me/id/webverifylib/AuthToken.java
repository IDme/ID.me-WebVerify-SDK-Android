package me.id.webverifylib;

import java.io.Serializable;
import java.util.Calendar;

import me.id.webverifylib.listener.IDmeScope;

public final class AuthToken implements Serializable {
  private String scopeId;
  private String accessToken;
  private String refreshToken;
  private Calendar accessTokenExpiration;
  private Calendar refreshTokenExpiration;

  public String getScopeId() {
    return scopeId;
  }

  public void setScopeId(IDmeScope scope) {
    scopeId = scope.getScopeId();
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

  public Calendar getAccessTokenExpiration() {
    return accessTokenExpiration;
  }

  public void setAccessTokenExpiration(Calendar accessTokenExpiration) {
    this.accessTokenExpiration = accessTokenExpiration;
  }

  public Calendar getRefreshTokenExpiration() {
    return refreshTokenExpiration;
  }

  public void setRefreshTokenExpiration(Calendar refreshTokenExpiration) {
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  private boolean isValidToken(String accessToken, Calendar accessTokenExpiration) {
    return accessToken != null && accessTokenExpiration != null && Calendar.getInstance().before(accessTokenExpiration);
  }

  public boolean isValidAccessToken() {
    return isValidToken(accessToken, accessTokenExpiration);
  }

  public boolean isValidRefreshToken() {
    return isValidToken(refreshToken, refreshTokenExpiration);
  }
}
