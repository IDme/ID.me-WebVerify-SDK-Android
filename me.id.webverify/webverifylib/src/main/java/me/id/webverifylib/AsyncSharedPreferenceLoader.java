package me.id.webverifylib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutionException;

final class AsyncSharedPreferenceLoader {
  private static final String IDME_SHARED_PREFERENCES_NAME = "IDmeSharedPreferences";

  private final Context context;

  private final AsyncTask<Void, Void, SharedPreferences> asyncTask = new AsyncTask<Void, Void, SharedPreferences>() {
    @Override
    protected SharedPreferences doInBackground(Void... voids) {
      return context.getSharedPreferences(IDME_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
  };

  AsyncSharedPreferenceLoader(Context context) {
    this.context = context;
    asyncTask.execute();
  }

  @Nullable
  SharedPreferences get() {
    try {
      return asyncTask.get();
    } catch (InterruptedException | ExecutionException ignored) {
      return null;
    }
  }
}
