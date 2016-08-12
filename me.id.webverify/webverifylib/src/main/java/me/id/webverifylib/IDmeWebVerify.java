package me.id.webverifylib;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class IDmeWebVerify
{
    //Scopes
    public final static String MILITARY = "military";
    public final static String STUDENT = "student";
    public final static String TEACHER = "teacher";
    public final static String GOVERNMENT = "government";
    public final static String FIRST_RESPONDER = "responder";

    public final static String IDME_WEB_VERIFY_RESPONSE = "response";
    public final static int WEB_REQUEST_CODE = 39820;

    private final String IDME_WEB_VERIFY_GET_AUTH_URI = "https://api.id.me/oauth/authorize?client_id=clientID&redirect_uri=redirectURI&response_type=token&scope=scopeType";
    private final String IDME_WEB_VERIFY_GET_USER_PROFILE = "https://api.id.me/api/public/v2/data.json?access_token=token";

    private String clientID = "";
    private String redirectURI = "";
    private String scope = "";
    private Activity activity;
    private boolean returnProperties = true;

    /**
     * Constructor For the class.
     *
     * @param clientID      The client ID provided by ID.me http://developer.id.me
     * @param redirectURI   The redirect URI
     * @param scope         The Verification type
     * @param activity      The calling activity
     */
    public IDmeWebVerify(String clientID, String redirectURI, String scope, Activity activity)
    {
        this.scope = scope;
        this.clientID = clientID;
        this.redirectURI = redirectURI;
        this.activity = activity;
    }

    /**
     * Constructor For the class.
     *
     * @param clientID         The client ID provided by ID.me http://developer.id.me
     * @param redirectURI      The redirect URI
     * @param scope            The Verification type
     * @param activity         The calling activity
     * @param returnProperties Whether user properties or access token should be returned
     */
    public IDmeWebVerify(String clientID, String redirectURI, String scope, Activity activity, boolean returnProperties)
    {
        this.scope = scope;
        this.clientID = clientID;
        this.redirectURI = redirectURI;
        this.activity = activity;
        this.returnProperties = returnProperties;
    }

    /**
     * Starts web view activity
     */
    public void StartWebView()
    {
        Intent intent = new Intent(activity, WebViewActivity.class);
        boolean start = true;
        if (clientID == null)
        {
            Toast.makeText(activity, "Client ID Cannot be null", Toast.LENGTH_SHORT).show();
            start = false;
        }

        if (redirectURI == null)
        {
            Toast.makeText(activity, "Redirect URI Cannot be null", Toast.LENGTH_SHORT).show();
            start = false;
        }


        if (start)
        {
            String url = createURL();
            intent.putExtra("URL", url);
            intent.putExtra("scope", scope);
            intent.putExtra("clientID", clientID);
            intent.putExtra("redirectURI", redirectURI);
            activity.startActivityForResult(intent, WEB_REQUEST_CODE);
        }
    }

    /**
     * Creates the url to be loaded in the webView
     *
     * @return URl with redirect uri, client id and scope
     */
    private String createURL()
    {
        String url = IDME_WEB_VERIFY_GET_AUTH_URI;
        url = url.replace("scopeType", scope);
        url = url.replace("redirectURI", redirectURI);
        url = url.replace("clientID", clientID);

        return url;
    }

    /**
     * Gets Web View Client
     *
     * @return Custom Web View Client
     */
    protected WebViewClient getWebViewClient()
    {
        return mWebViewClient;
    }


    private WebViewClient mWebViewClient = new WebViewClient()
    {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {

            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, final String url)
        {
            boolean hasToken = url.contains("access_token=");
            if (hasToken)
            {
                final String accessToken = ExtractAccessToken(url);

                if (returnProperties)
                {
                    AsyncTask asyncTask = new AsyncTask()
                    {
                        @Override protected Object doInBackground(Object[] params)
                        {
                            GetWebProfile(CreateRequestUrl(accessToken));
                            return null;
                        }
                    };
                    asyncTask.execute(null, null, null);
                } else {
                    SendDataBack(accessToken);
                }
            }
        }
    };

    /**
     * Creates the URL for the Web Request
     *
     * @return URL with proper formatted request
     */
    private String CreateRequestUrl(String accessToken)
    {
        String url = IDME_WEB_VERIFY_GET_USER_PROFILE;

        url = url.replace("token", accessToken);

        return url;
    }

    /**
     * Extract Access Token from URL
     *
     * @param url URL that contains access token
     */
    private String ExtractAccessToken(String url)
    {

        int firstIndex = url.indexOf("=") + 1;
        int lastIndex = url.indexOf("&");
        return url.substring(firstIndex, lastIndex);

    }

    /**
     * This performs the Web Request
     *
     * @param url URL for the Web Request
     */
    private void GetWebProfile(String url)
    {

        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();

                out.close();
                SendDataBack(responseString);

            }
            else
            {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e)
        {
            SendDataBack(e.getMessage());
            Log.e("Web Request Error", e.getMessage());
        }
    }

    /**
     * Sends data back to the activity that called the WebView.
     *
     * @param response The response form the Web Request.
     */
    private void SendDataBack(String response)
    {
        activity.getIntent().putExtra(IDME_WEB_VERIFY_RESPONSE, response);
        activity.setResult(Activity.RESULT_OK, activity.getIntent());

        activity.finish();
    }


}
