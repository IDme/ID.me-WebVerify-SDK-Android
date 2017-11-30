package me.id.webverifylib;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import me.id.webverifylib.exception.IDmeException;
import me.id.webverifylib.listener.IDmeAccessTokenManagerListener;
import me.id.webverifylib.networking.GetAccessTokenConnectionTask;

public class RedirectUriReceiverActivity extends Activity {
  private final IDmeAccessTokenManagerListener authCodeListener = new IDmeAccessTokenManagerListener() {
    @Override
    public void onSuccess(AuthToken authToken) {
      IDmeWebVerify.getInstance().notifySuccess(authToken);
      sendResult(RESULT_OK);
    }

    @Override
    public void onError(Throwable throwable) {
      IDmeWebVerify.getInstance().notifyFailure(throwable);
      sendResult(RESULT_CANCELED);
    }
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    State currentState = IDmeWebVerify.getCurrentState();

    if (currentState == null) {
      IDmeWebVerify.getInstance().notifyFailure(new IDmeException("Activity was created but there is not an initialized process"));
      sendResult(RESULT_CANCELED);
      return;
    }

    if (getIntent() == null || getIntent().getData() == null) {
      IDmeWebVerify.getInstance().notifyFailure(new IDmeException("Null intent or invalid data was received"));
      sendResult(RESULT_CANCELED);
      return;
    }

    if (currentState == State.LOGOUT) {
      IDmeWebVerify.getInstance().notifyLogoutSuccess();
      sendResult(RESULT_OK);
      return;
    }

    String code = getIntent().getData().getQueryParameter(IDmeWebVerify.PARAM_CODE);
    if (code == null || code.isEmpty()) {
      IDmeWebVerify.getInstance().notifyFailure(new IDmeException("An error has occurred getting the auth token"));
      sendResult(RESULT_CANCELED);
    } else {
      new GetAccessTokenConnectionTask(IDmeWebVerify.getAccessTokenQuery(code), authCodeListener, currentState.getScope())
          .execute(IDmeWebVerify.getIdMeWebVerifyAccessTokenUri());
    }
  }

  private void sendResult(int resultCode) {
    setResult(resultCode);
    try {
      // MOB-944: send an intent to the activity that started the browser. This is needed due to 
      // RedirectUriReceiverActivity is opened in the browser's stack, so when this activity finishes,
      // the result is that the browser will stay visible.
      Intent intent = new Intent(this, IDmeCustomTabsActivity.class);
      PendingIntent.getActivity(this, 0, intent, 0).send();
    } catch (PendingIntent.CanceledException ex) {
      ex.printStackTrace();
    }
    finish();
  }
}
