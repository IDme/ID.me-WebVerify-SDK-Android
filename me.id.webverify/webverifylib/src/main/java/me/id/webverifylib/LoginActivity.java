package me.id.webverifylib;

import android.support.annotation.NonNull;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.id.webverifylib.listener.AuthenticationFinishedListener;
import me.id.webverifylib.listener.IDmeAccessTokenManagerListener;
import me.id.webverifylib.listener.IDmeGetAuthCodeListener;
import me.id.webverifylib.listener.IDmePageFinishedListener;
import me.id.webverifylib.listener.IDmeScope;
import me.id.webverifylib.networking.GetAccessTokenConnectionTask;

public class LoginActivity extends WebViewActivity {
  private final IDmeAccessTokenManagerListener authCodeListener = new IDmeAccessTokenManagerListener() {
    @Override
    public void onSuccess(AuthToken authToken) {
      IDmeWebVerify.getInstance().getAccessTokenManagerListener().onSuccess(authToken);
      finish();
    }

    @Override
    public void onError(Throwable throwable) {
      IDmeWebVerify.getInstance().getAccessTokenManagerListener().onError(throwable);
    }
  };

  private final IDmeGetAuthCodeListener iDmeGetAuthCodeListener = new IDmeGetAuthCodeListener() {
    @Override
    public void onSuccess(String authCode) {
      showLoadingMessage();
      new GetAccessTokenConnectionTask(IDmeWebVerify.getAccessTokenQuery(authCode), authCodeListener, scope)
          .execute(IDmeWebVerify.getIdMeWebVerifyAccessTokenUri());
    }

    @Override
    public void onError(Throwable throwable) {
      IDmeWebVerify.getInstance().getAccessTokenManagerListener().onError(throwable);
    }
  };

  public void showLoadingMessage() {
    // TODO mirland 22/02/17:
  }

  @NonNull
  protected WebViewClient getWebClient(IDmeScope scope) {
    return new LoginWebViewClient(scope, new AuthenticationFinishedListener(
        iDmeGetAuthCodeListener,
        IDmeWebVerify.getRedirectUri()
    ));
  }

  protected class LoginWebViewClient extends IDmeWebViewClient {
    LoginWebViewClient(IDmeScope scope, IDmePageFinishedListener listener) {
      super(scope, listener);
    }

    @Override
    protected void onCallbackCalled(WebView view, final String url) {
      listener.onCallbackResponse(url, scope);
    }
  }
}
