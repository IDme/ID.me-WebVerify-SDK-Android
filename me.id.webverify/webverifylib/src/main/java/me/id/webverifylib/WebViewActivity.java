package me.id.webverifylib;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.CookieManager;
import android.webkit.WebView;

public class WebViewActivity extends AppCompatActivity {
  public static final String EXTRA_SCOPE = "scope";
  public static final String EXTRA_URL = "Url";
  public static final String EXTRA_CLIENT_ID = "clientId";
  public static final String EXTRA_REDIRECT_URI = "redirectUri";
  public static final String EXTRA_RETURN_PROPERTIES = "returnProperties";

  private IDmeWebVerify iDmeWebVerify;
  private WebView webView;
  private Toolbar toolbar;

  @Override
  @SuppressLint("SetJavaScriptEnabled")
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);

    IDmeScope scope = (IDmeScope) getIntent().getSerializableExtra(EXTRA_SCOPE);
    String url = getIntent().getStringExtra(EXTRA_URL);
    String clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
    String redirectUri = getIntent().getStringExtra(EXTRA_REDIRECT_URI);
    boolean returnProperties = getIntent().getBooleanExtra(EXTRA_RETURN_PROPERTIES, true);

    iDmeWebVerify = new IDmeWebVerify(clientId, redirectUri, scope, this, returnProperties);

    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    webView = (WebView) findViewById(R.id.webView);
    webView.setWebViewClient(iDmeWebVerify.getWebViewClient());
    webView.loadUrl(url);
    webView.getSettings().setJavaScriptEnabled(true);
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
  }

  @Override
  public void onBackPressed() {
    if (webView.canGoBack()) {
      webView.goBack();
    } else {
      super.onBackPressed();
    }
  }
}
