package me.id.webverifylib;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;


public class WebViewActivity extends ActionBarActivity
{
    IDmeWebVerify iDmeWebVerify;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        String scope = getIntent().getStringExtra("affiliationType");
        String url = getIntent().getStringExtra("URL");
        String clientId = getIntent().getStringExtra("clientID");
        String redirectUri = getIntent().getStringExtra("redirectURI");

        iDmeWebVerify = new IDmeWebVerify(clientId, redirectUri, scope, this);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(iDmeWebVerify.getWebViewClient());
        webView.loadUrl(url);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        CookieManager cookieManager = CookieManager.getInstance();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            cookieManager.removeAllCookie();
        }
        else
        {
            cookieManager.removeAllCookies(null);
        }
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
        webView.destroy();

    }
}
