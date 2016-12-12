package me.id.webverifylib;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.webkit.CookieManager;
import android.webkit.WebView;


public class WebViewActivity extends ActionBarActivity
{
    IDmeWebVerify iDmeWebVerify;
    private WebView webView;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);


        getSupportActionBar().setTitle(Html.fromHtml("<font color='#2fc073'>Verify With ID.me </font>"));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff2e3d50));
        String scope = getIntent().getStringExtra("scope");
        String url = getIntent().getStringExtra("URL");
        String clientId = getIntent().getStringExtra("clientID");
        String redirectUri = getIntent().getStringExtra("redirectURI");
        boolean returnProperties = getIntent().getBooleanExtra("returnProperties", true);

        iDmeWebVerify = new IDmeWebVerify(clientId, redirectUri, scope, this, returnProperties);

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(iDmeWebVerify.getWebViewClient());
        webView.loadUrl(url);
        webView.getSettings().setJavaScriptEnabled(true);
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

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
