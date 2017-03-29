package me.id.webverifylib.networking;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.id.webverifylib.AuthToken;
import me.id.webverifylib.exception.IDmeException;
import me.id.webverifylib.exception.UnauthenticatedException;
import me.id.webverifylib.helper.AccessTokenHelper;
import me.id.webverifylib.helper.ObjectHelper;
import me.id.webverifylib.listener.IDmeAccessTokenManagerListener;
import me.id.webverifylib.listener.IDmeScope;

/**
 * Created by remer on 3/2/17.
 */
public final class GetAccessTokenConnectionTask extends AsyncTask<String, Void, String> {
  @NonNull
  private final IDmeAccessTokenManagerListener listener;
  private final String query;
  private final IDmeScope scope;
  private boolean returnedError;

  public GetAccessTokenConnectionTask(String query, @NonNull IDmeAccessTokenManagerListener listener, IDmeScope scope) {
    this.query = query;
    this.listener = listener;
    this.scope = scope;
  }

  @Override
  protected String doInBackground(String... urls) {
    HttpURLConnection urlConnection = null;
    URL urlRequest;
    try {
      urlRequest = new URL(urls[0]);
      urlConnection = (HttpURLConnection) urlRequest.openConnection();
      byte[] outputBytes = query.getBytes("UTF-8");

      urlConnection.setRequestMethod("POST");
      urlConnection.setDoOutput(true);
      urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      OutputStream outputStream = urlConnection.getOutputStream();
      outputStream.write(outputBytes);
      outputStream.close();
      int responseCode = urlConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        return ObjectHelper.readStream(urlConnection.getInputStream());
      } else {
        String errorMessage = ObjectHelper.readStream(urlConnection.getErrorStream());
        returnedError = true;
        listener.onError(new UnauthenticatedException(errorMessage));
      }
    } catch (Exception exception) {
      returnedError = true;
      listener.onError(exception);
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
    return null;
  }

  @Override
  protected void onPostExecute(String result) {
    if (returnedError) {
      return;
    }
    if (result == null) {
      listener.onError(new IDmeException("Get Access Token error"));
    } else {
      AuthToken authToken = AccessTokenHelper.extractAccessTokenFromJson(scope, result);
      if (authToken == null) {
        listener.onError(new IDmeException("Get Access Token error"));
      } else {
        listener.onSuccess(authToken);
      }
    }
  }
}
