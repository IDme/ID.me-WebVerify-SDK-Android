package me.id.webverifylib;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.id.webverifylib.exception.UserCanceledException;
import me.id.webverifylib.listener.IDmePageFinishedListener;
import me.id.webverifylib.listener.IDmeScope;

public class WebViewActivity extends AppCompatActivity {
  public static final String EXTRA_SCOPE_ID = "scope";
  public static final String EXTRA_URL = "url";

  protected WebView webView;
  protected IDmeScope scope;
  protected String url;

  @Override
  @SuppressLint("SetJavaScriptEnabled")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);

    scope = getScopeFromKey(getIntent().getStringExtra(EXTRA_SCOPE_ID));
    url = getIntent().getStringExtra(EXTRA_URL);

    webView = (WebView) findViewById(R.id.webView);
    webView.setWebViewClient(getWebClient(scope));
    webView.loadUrl(url);
    webView.getSettings().setJavaScriptEnabled(true);
  }

  @NonNull
  protected WebViewClient getWebClient(IDmeScope scope) {
    return new IDmeWebViewClient(scope, IDmeWebVerify.getInstance().getPageFinishedListener());
  }

  @NonNull
  private IDmeScope getScopeFromKey(final String scopeId) {
    return new IDmeScope() {
      @NonNull
      @Override
      public String getScopeId() {
        return scopeId;
      }
    };
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    CookieManager cookieManager = CookieManager.getInstance();

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      //noinspection deprecation
      cookieManager.removeAllCookie();
    } else {
      cookieManager.removeAllCookies(null);
    }
    webView.clearCache(true);
    webView.clearHistory();
    webView.clearFormData();
    webView.destroy();
    IDmeWebVerify.getInstance().clearSignInListener();
  }

  @Override
  public void onBackPressed() {
    if (webView.canGoBack()) {
      webView.goBack();
    } else {
      IDmeWebVerify.getInstance().notifyFailure(new UserCanceledException());
      super.onBackPressed();
    }
  }

  protected class IDmeWebViewClient extends WebViewClient {
    protected final IDmeScope scope;
    protected final IDmePageFinishedListener listener;

    IDmeWebViewClient(IDmeScope scope, IDmePageFinishedListener listener) {
      this.scope = scope;
      this.listener = listener;
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
      if (listener.isCallbackUrl(url)) {
        listener.onCallbackResponse(url, scope);
        finish();
      }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return listener.isCallbackUrl(url);
    }
  }
}
