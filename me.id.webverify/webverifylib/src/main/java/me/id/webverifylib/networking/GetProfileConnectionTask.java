package me.id.webverifylib.networking;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import me.id.webverifylib.IDmeProfile;
import me.id.webverifylib.IDmeWebVerify;
import me.id.webverifylib.exception.IDmeException;
import me.id.webverifylib.listener.IDmeGetProfileListener;

/**
 * Created by remer on 3/2/17.
 */
public final class GetProfileConnectionTask extends AsyncTask<String, Void, String> {
  @NonNull
  private final IDmeGetProfileListener listener;
  private boolean returnedError;

  public GetProfileConnectionTask(@NonNull IDmeGetProfileListener listener) {
    this.listener = listener;
  }

  @Override
  protected String doInBackground(String... urls) {
    HttpURLConnection urlConnection = null;
    URL urlRequest;
    try {
      urlRequest = new URL(urls[0]);
      urlConnection = (HttpURLConnection) urlRequest.openConnection();

      int responseCode = urlConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        return readStream(urlConnection.getInputStream());
      }
    } catch (IOException exception) {
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
      listener.onError(new IDmeException("Profile error"));
    } else {
      try {
        listener.onSuccess(new IDmeProfile(result));
      } catch (JSONException e) {
        listener.onError(e);
      }
    }
  }

  /**
   * This converts the InputStream to a String
   *
   * @param inputStream from the Web Request
   * @return the converted string
   */
  private static String readStream(InputStream inputStream) throws IOException {
    BufferedReader reader = null;
    StringBuilder response = new StringBuilder();
    try {
      reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException exception) {
          Log.e(IDmeWebVerify.TAG, "Read stream error", exception);
        }
      }
    }
    return response.toString();
  }
}
