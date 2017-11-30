package me.id.webverifylib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by remer on 30/11/17.
 */
public class IDmePreferences {
  private static final String APP_NAME_KEY_NAME = "IDmeWebVerify.applicationName";

  @Nullable
  private SharedPreferences preferences;
  @Nullable
  private AsyncSharedPreferenceLoader preferenceLoader;
  @NonNull
  private final Context context;

  public IDmePreferences(Context context) {
    this.context = context;
  }

  void storeApplicationNameFromContext(@NonNull Context context) {
    loadPreferencesIfNeeded(context);
    if (preferences == null) {
      return;
    }

    ApplicationInfo applicationInfo = context.getApplicationInfo();
    int stringId = applicationInfo.labelRes;
    String appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    preferences.edit()
        .putString(APP_NAME_KEY_NAME, appName)
        .apply();
  }

  @Nullable
  String loadApplicationName(@NonNull Context context) {
    loadPreferencesIfNeeded(context);
    if (preferences == null) {
      return null;
    }

    return preferences.getString(APP_NAME_KEY_NAME, null);
  }

  private void loadPreferencesIfNeeded(Context context) {
    if (preferenceLoader == null) {
      try {
        preferenceLoader = new AsyncSharedPreferenceLoader(context);
      } catch (Exception ex) {
        Log.w(IDmeWebVerify.TAG, "Failed to load shared preferences", ex);
        return;
      }
    }
    if (preferences == null) {
      preferences = preferenceLoader.get();
    }
  }
}
