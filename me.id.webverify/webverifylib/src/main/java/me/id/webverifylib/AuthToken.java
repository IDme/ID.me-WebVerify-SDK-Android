package me.id.webverifylib;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mirland on 13/12/16.
 */
public class AuthToken implements Serializable {
  private IDmeScope scope;
  private String accessToken;
  private String refreshToken;
  private Date expiration;

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

  public Date getExpiration() {
    return expiration;
  }

  public void setExpiration(Date expiration) {
    this.expiration = expiration;
  }
}
