package me.id.meidwebverify;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import me.id.webverifylib.IDmeCommonScope;
import me.id.webverifylib.IDmeGetAccessTokenListener;
import me.id.webverifylib.IDmeGetProfileListener;
import me.id.webverifylib.IDmeProfile;
import me.id.webverifylib.IDmeScope;
import me.id.webverifylib.IDmeWebVerify;

public class MainActivity extends ActionBarActivity {
  private String clientID = null;
  private String redirectUri = null;
  private boolean returnProperties = true;
  private TextView txtResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    txtResult = (TextView) findViewById(R.id.txtResult);
    IDmeWebVerify.initialize(this, clientID, redirectUri);
    Button btnVerify = (Button) findViewById(R.id.btnVerify);
    btnVerify.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        verify();
      }
    });
  }

  /**
   * Method that Starts the Verification Process.
   */
  public void verify() {
    Spinner propRoute = (Spinner) findViewById(R.id.spnProperties);

    returnProperties = propRoute.getSelectedItem().toString().equals("Yes");

    final IDmeScope affiliationType = getSelectedAffiliationType();
    if (affiliationType == null) {
      showError(new IllegalStateException("Affiliation Type is required"));
      return;
    }
    IDmeWebVerify.getInstance().getAccessToken(affiliationType, new IDmeGetAccessTokenListener() {
      @Override
      public void onSuccess(String accessToken) {
        if (returnProperties) {
          showUserProfileInformation(affiliationType);
        } else {
          showResponse(accessToken);
        }
      }

      @Override
      public void onError(Throwable throwable) {
        showError(throwable);
      }
    });
  }

  @Nullable
  private IDmeScope getSelectedAffiliationType() {
    Spinner spnRoute = (Spinner) findViewById(R.id.spnRoute);
    Object selectedItem = spnRoute.getSelectedItem();
    if (selectedItem == null) {
      return null;
    } else if (selectedItem.toString().equals("Military")) {
      return IDmeCommonScope.MILITARY;
    } else if (selectedItem.toString().equals("Student")) {
      return IDmeCommonScope.STUDENT;
    } else if (selectedItem.toString().equals("Teacher")) {
      return IDmeCommonScope.TEACHER;
    } else if (selectedItem.toString().equals("First Responder")) {
      return IDmeCommonScope.FIRST_RESPONDER;
    } else if (selectedItem.toString().equals("Government")) {
      return IDmeCommonScope.GOVERNMENT;
    }
    return null;
  }

  private void showError(Throwable throwable) {
    throwable.printStackTrace();
    Toast.makeText(MainActivity.this, throwable.getMessage(), Toast.LENGTH_LONG).show();
  }

  private void showResponse(Object object) {
    txtResult.setText(String.format(Locale.getDefault(), "Response : %s", object));
  }

  public void showUserProfileInformation(IDmeScope scope) {
    IDmeWebVerify.getInstance().getUserProfile(scope, new IDmeGetProfileListener() {
      @Override
      public void onSuccess(IDmeProfile profile) {
        showResponse(profile);
      }

      @Override
      public void onError(Throwable throwable) {
        showError(throwable);
      }
    });
  }
}
