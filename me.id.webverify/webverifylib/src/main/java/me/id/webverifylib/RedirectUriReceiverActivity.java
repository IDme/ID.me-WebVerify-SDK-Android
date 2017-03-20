package me.id.webverifylib;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import me.id.webverifylib.exception.IDmeException;
import me.id.webverifylib.listener.IDmeAccessTokenManagerListener;
import me.id.webverifylib.networking.GetAccessTokenConnectionTask;

public class RedirectUriReceiverActivity extends Activity {
  private WebView progressBarWebView;

  private final IDmeAccessTokenManagerListener authCodeListener = new IDmeAccessTokenManagerListener() {
    @Override
    public void onSuccess(AuthToken authToken) {
      IDmeWebVerify.getInstance().notifySuccess(authToken);
      sendResult(Activity.RESULT_OK);
    }

    @Override
    public void onError(Throwable throwable) {
      IDmeWebVerify.getInstance().notifyFailure(throwable);
      sendResult(Activity.RESULT_CANCELED);
    }
  };

  private void initializeProgressBar() {
    progressBarWebView = (WebView) findViewById(R.id.progress_bar_webView);
    progressBarWebView.getSettings().setLoadWithOverviewMode(true);
    progressBarWebView.getSettings().setUseWideViewPort(true);
    progressBarWebView.loadUrl("file:///android_asset/image/spinner.gif");
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_loading);
    initializeProgressBar();
    State currentState = IDmeWebVerify.getCurrentState();
    if (currentState == null) {
      throw new IDmeException("Current state cannot be null");
    }

    String code = getIntent().getData().getQueryParameter(IDmeWebVerify.PARAM_CODE);
    if (code == null || code.isEmpty()) {
      IDmeWebVerify.getInstance().notifyFailure(new IDmeException("An error has occurred getting the auth token"));
      sendResult(Activity.RESULT_CANCELED);
    } else {
      new GetAccessTokenConnectionTask(IDmeWebVerify.getAccessTokenQuery(code), authCodeListener, currentState.getScope())
          .execute(IDmeWebVerify.getIdMeWebVerifyAccessTokenUri());
    }
  }

  private void sendResult(int resultCode) {
    setResult(resultCode);
    finish();
  }
}
