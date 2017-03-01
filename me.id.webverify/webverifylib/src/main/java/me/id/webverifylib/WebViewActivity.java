package me.id.webverifylib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import me.id.webverifylib.exception.UserCanceledException;
import me.id.webverifylib.listener.IDmePageFinishedListener;
import me.id.webverifylib.listener.IDmeScope;

public class WebViewActivity extends AppCompatActivity {
  public static final String EXTRA_SCOPE_ID = "scope";
  public static final String EXTRA_URL = "url";

  protected WebView webView;
  protected Toolbar toolbar;
  protected WebView progressBarWebView;
  protected Button networkErrorButton;
  protected View internetConnectionErrorView;
  protected IDmeScope scope;
  protected String url;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);

    scope = getScopeFromKey(getIntent().getStringExtra(EXTRA_SCOPE_ID));
    url = getIntent().getStringExtra(EXTRA_URL);

    initializeNoInternetConnectionView();
    initializeProgressBar();
    initializeToolbar();
    initializeWebView();
  }

  @SuppressLint("SetJavaScriptEnabled")
  private void initializeWebView() {
    webView = (WebView) findViewById(R.id.webView);
    webView.getSettings().setAppCacheEnabled(false);
    webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    webView.getSettings().setJavaScriptEnabled(true);
    clearWebViewCacheAndHistory();
    webView.setWebViewClient(getWebClient(scope));
    webView.loadUrl(url);
  }

  private void initializeToolbar() {
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    //noinspection ConstantConditions
    getSupportActionBar().setTitle("");
    supportInvalidateOptionsMenu();
  }

  private void hideProgressBar(int startDelayInSeconds) {
    if (progressBarWebView != null) {
      progressBarWebView.animate()
          .alpha(0.0f)
          .setDuration(1000)
          .setStartDelay(startDelayInSeconds * 1000)
          .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              progressBarWebView.setVisibility(View.GONE);
            }
          });
    }
  }

  private void hideInternetConnectionErrorView(int startDelayInSeconds) {
    if (progressBarWebView != null) {
      internetConnectionErrorView.animate()
          .alpha(0.0f)
          .setDuration(500)
          .setStartDelay(startDelayInSeconds * 1000)
          .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              progressBarWebView.setVisibility(View.GONE);
            }
          });
    }
  }

  private void initializeProgressBar() {
    progressBarWebView = (WebView) findViewById(R.id.progress_bar_webView);
    progressBarWebView.getSettings().setLoadWithOverviewMode(true);
    progressBarWebView.getSettings().setUseWideViewPort(true);
    progressBarWebView.setBackgroundColor(Color.TRANSPARENT);
    progressBarWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
    progressBarWebView.loadUrl("file:///android_asset/image/spinner.gif");
  }

  private void initializeNoInternetConnectionView() {
    internetConnectionErrorView = findViewById(R.id.internet_connection_error_layout);
    networkErrorButton = (Button) findViewById(R.id.network_error_button);
    networkErrorButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        webView.reload();
        progressBarWebView.setAlpha(1);
        progressBarWebView.setVisibility(View.VISIBLE);
        hideInternetConnectionErrorView(4);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.close) {
      finish();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
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
    clearWebViewCacheAndHistory();
    webView.destroy();
    IDmeWebVerify.getInstance().clearSignInListener();
  }

  protected void clearWebViewCacheAndHistory() {
    webView.clearCache(true);
    webView.clearHistory();
    webView.clearFormData();
    CookieManager cookieManager = CookieManager.getInstance();
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      //noinspection deprecation
      cookieManager.removeAllCookie();
    } else {
      cookieManager.removeAllCookies(null);
    }
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
      if (internetConnectionErrorView.getVisibility() == View.GONE) {
        hideProgressBar(1);
      }
    }

    protected void onCallbackCalled(WebView view, final String url) {
      listener.onCallbackResponse(url, scope);
      finish();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      switch (errorCode) {
        case WebViewClient.ERROR_AUTHENTICATION:
        case WebViewClient.ERROR_HOST_LOOKUP:
        case WebViewClient.ERROR_BAD_URL:
        case WebViewClient.ERROR_CONNECT:
        case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
        case WebViewClient.ERROR_IO:
        case WebViewClient.ERROR_PROXY_AUTHENTICATION:
        case WebViewClient.ERROR_TIMEOUT:
        case WebViewClient.ERROR_UNKNOWN:
          internetConnectionErrorView.clearAnimation();
          internetConnectionErrorView.animate().cancel();
          internetConnectionErrorView.setAlpha(1);
          internetConnectionErrorView.setVisibility(View.VISIBLE);
          hideProgressBar(0);
        default:
          super.onReceivedError(view, errorCode, description, failingUrl);
      }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
      super.onReceivedError(view, request, error);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (listener.isCallbackUrl(url)) {
        onCallbackCalled(view, url);
        return true;
      }
      return false;
    }
  }
}
