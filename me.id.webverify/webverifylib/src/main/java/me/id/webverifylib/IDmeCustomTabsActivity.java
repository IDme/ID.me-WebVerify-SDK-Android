package me.id.webverifylib;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import me.id.webverifylib.exception.UserCanceledException;
import me.id.webverifylib.helper.CustomTabsHelper;

public class IDmeCustomTabsActivity extends Activity {
  public static final String EXTRA_URL = "url";

  private boolean shouldCloseCustomTab = true;
  protected String url;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    url = getIntent().getStringExtra(EXTRA_URL);

    if (url == null) {
      // MOB-1029: if coming with not current state, url will be null and crash the app
      Log.d(IDmeWebVerify.TAG, "Null url value received");
      finish();
      return;
    }

    if (savedInstanceState == null) {
      shouldCloseCustomTab = false;
      try {
        CustomTabsHelper.getCustomTabIntent(this, url)
            .launchUrl(this, Uri.parse(url));
      } catch (ActivityNotFoundException exception) {
        IDmeWebVerify.getInstance().notifyFailure(
            new ActivityNotFoundException("There isn't an available browser to handle the ID.me oauth flow")
        );
        finish();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (shouldCloseCustomTab) {
      if (IDmeWebVerify.getCurrentState() != null) {
        IDmeWebVerify.getInstance().notifyFailure(new UserCanceledException());
      }
      finish();
    }
    shouldCloseCustomTab = true;
  }
}
