package me.id.webverifylib;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

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

    if (savedInstanceState == null) {
      shouldCloseCustomTab = false;
      CustomTabsHelper.getCustomTabIntent(this, url)
          .launchUrl(this, Uri.parse(url));
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
