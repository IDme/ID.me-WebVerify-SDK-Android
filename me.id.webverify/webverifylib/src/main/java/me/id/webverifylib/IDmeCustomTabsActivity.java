package me.id.webverifylib;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import me.id.webverifylib.exception.UserCanceledException;
import me.id.webverifylib.helper.CustomTabsHelper;

public class IDmeCustomTabsActivity extends Activity {
  private static final int CANCEL_RESULT_CALLBACK_DELAY_IN_MILLIS = 300;
  public static final String EXTRA_URL = "url";

  private boolean shouldCloseCustomTab = true;
  protected String url;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    url = getIntent().getStringExtra(EXTRA_URL);
    if (savedInstanceState == null) {
      if (url == null) {
        Log.w(IDmeWebVerify.TAG, "Url cannot be null, the browser is closed");
        finish();
      } else {
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
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (shouldCloseCustomTab) {
      // In some devices, when the callback url isCalled, this activity is resumed before the
      // `RedirectUriReceiverActivity` is invoked (the callback activity), producing a cancellation state.
      // The delay is done to check if an activity callback was queued.
      new Handler().postDelayed(() -> {
        if (IDmeWebVerify.getCurrentState() != null && !IDmeWebVerify.isExecutingBackgroundTaskState()) {
          IDmeWebVerify.getInstance().notifyFailure(new UserCanceledException());
        }
        if (!isDestroyed() && !IDmeWebVerify.isExecutingBackgroundTaskState()) {
          finish();
        }
      }, CANCEL_RESULT_CALLBACK_DELAY_IN_MILLIS);
    }
    shouldCloseCustomTab = true;
  }
}
