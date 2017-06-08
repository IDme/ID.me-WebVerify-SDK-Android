package me.id.webverifylib.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import me.id.webverifylib.AuthToken;
import me.id.webverifylib.listener.IDmeScope;

/**
 * Created by mirland on 22/02/17.
 */
public class AccessTokenHelper {
  private static final String ACCESS_TOKEN_KEY = "access_token";
  private static final String ACCESS_TOKEN_EXPIRE_KEY = "expires_in";
  private static final String REFRESH_TOKEN_KEY = "refresh_token";
  private static final String REFRESH_TOKEN_EXPIRE_KEY = "refresh_expires_in";

  /**
   * Extracts the access token from a json response
   *
   * @param json Json that contains access token
   */
  @Nullable
  public static AuthToken extractAccessTokenFromJson(@NonNull IDmeScope scope, @NonNull String json) {
    try {
      JSONObject jsonObject = new JSONObject(json);
      AuthToken authToken = new AuthToken();
      authToken.setScopeId(scope.getScopeId());
      authToken.setAccessToken(jsonObject.getString(ACCESS_TOKEN_KEY));
      authToken.setRefreshToken(jsonObject.getString(REFRESH_TOKEN_KEY));
      try {
        Calendar expirationDate = getExpirationDate(jsonObject.getString(ACCESS_TOKEN_EXPIRE_KEY));
        authToken.setAccessTokenExpiration(expirationDate);
        expirationDate = getExpirationDate(jsonObject.getString(REFRESH_TOKEN_EXPIRE_KEY));
        authToken.setRefreshTokenExpiration(expirationDate);
      } catch (NumberFormatException ex) {
        ex.printStackTrace();
      }
      return authToken;
    } catch (JSONException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  @NonNull
  private static Calendar getExpirationDate(String expirationTime) {
    Integer expirationTimeInSeconds = Integer.valueOf(expirationTime);
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.SECOND, expirationTimeInSeconds);
    return calendar;
  }
}
