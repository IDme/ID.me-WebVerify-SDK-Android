package me.id.webverifylib;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {
  public static final String EXTRA_SCOPE_ID = "scope";
  public static final String EXTRA_URL = "Url";

  private WebView webView;
  private Toolbar toolbar;

  @Override
  @SuppressLint("SetJavaScriptEnabled")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);

    IDmeScope scope = getScopeFromKey(getIntent().getStringExtra(EXTRA_SCOPE_ID));
    String url = getIntent().getStringExtra(EXTRA_URL);

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    webView = (WebView) findViewById(R.id.webView);
    webView.setWebViewClient(new IDmeWebViewClient(scope));
    webView.loadUrl(url);
    webView.getSettings().setJavaScriptEnabled(true);
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
      super.onBackPressed();
    }
  }

  private class IDmeWebViewClient extends WebViewClient {
    private final IDmeScope scope;

    IDmeWebViewClient(IDmeScope scope) {
      this.scope = scope;
    }

    @Override
    public void onPageFinished(WebView view, final String url) {
      if (IDmeWebVerify.getInstance().validateAndSaveAccessToken(url, scope)) {
        IDmeWebVerify.getInstance().notifyAccessToken(scope);
        finish();
      }
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldOverrideUrlLoading(WebView view, String url){
      return IDmeWebVerify.getInstance().hasAccessToken(url);
    }
  }
}
