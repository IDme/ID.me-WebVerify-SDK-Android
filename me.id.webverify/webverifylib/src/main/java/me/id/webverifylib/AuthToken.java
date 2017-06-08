package me.id.webverifylib;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Auth Token representation.
 */
public final class AuthToken implements Serializable {
  private String scopeId;
  private String accessToken;
  private String refreshToken;
  private Calendar accessTokenExpiration;
  private Calendar refreshTokenExpiration;
  private boolean wasForcedlyInvalidated;

  String getScopeId() {
    return scopeId;
  }

  public void setScopeId(String scopeId) {
    this.scopeId = scopeId;
  }

  String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void setAccessTokenExpiration(Calendar accessTokenExpiration) {
    this.accessTokenExpiration = accessTokenExpiration;
  }

  public void setRefreshTokenExpiration(Calendar refreshTokenExpiration) {
    this.refreshTokenExpiration = refreshTokenExpiration;
  }

  private boolean isValidToken(String accessToken, Calendar accessTokenExpiration) {
    return accessToken != null && accessTokenExpiration != null && Calendar.getInstance().before(accessTokenExpiration);
  }

  boolean isValidAccessToken() {
    return !wasForcedlyInvalidated && isValidToken(accessToken, accessTokenExpiration);
  }

  boolean isValidRefreshToken() {
    return isValidToken(refreshToken, refreshTokenExpiration);
  }

  void invalidateAccessToken() {
    wasForcedlyInvalidated = true;
  }
}
