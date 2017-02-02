package me.id.meidwebverify;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import me.id.webverifylib.IDmeAffiliationType;
import me.id.webverifylib.IDmeCommonScope;
import me.id.webverifylib.IDmeGetAccessTokenListener;
import me.id.webverifylib.IDmeGetProfileListener;
import me.id.webverifylib.IDmeProfile;
import me.id.webverifylib.IDmeRegisterAffiliationListener;
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

    IDmeWebVerify.initialize(this, clientID, redirectUri);

    setContentView(R.layout.activity_main);

    txtResult = (TextView) findViewById(R.id.txtResult);

    Button btnVerify = (Button) findViewById(R.id.btnVerify);
    btnVerify.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        verify();
      }
    });

    Button btnLogin = (Button) findViewById(R.id.btnLogin);
    btnLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        login();
      }
    });
  }

  /**
   * Method that starts de authentication process
   */
  public void login() {
    IDmeWebVerify.getInstance().login(this, IDmeCommonScope.WALLET, new IDmeGetAccessTokenListener() {
      @Override
      public void onSuccess(String accessToken) {
        if (returnProperties) {
          showUserProfileInformation(IDmeCommonScope.WALLET);
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

  /**
   * Method that starts the process of adding a new Affiliation type
   */
  public void verify() {
    Spinner propRoute = (Spinner) findViewById(R.id.spnProperties);

    returnProperties = propRoute.getSelectedItem().toString().equals("Yes");

    final IDmeAffiliationType affiliationType = getSelectedAffiliationType();
    if (affiliationType == null) {
      showError(new IllegalStateException("Affiliation Type is required"));
      return;
    }
    IDmeWebVerify.getInstance().registerAffiliation(this, IDmeCommonScope.WALLET, affiliationType, new IDmeRegisterAffiliationListener() {
      @Override
      public void onSuccess() {
        Toast.makeText(MainActivity.this, "Affiliation " + affiliationType + " was correctly added", Toast.LENGTH_LONG).show();
      }

      @Override
      public void onError(Throwable throwable) {
        showError(throwable);
      }
    });
  }

  @Nullable
  private IDmeAffiliationType getSelectedAffiliationType() {
    Spinner spnRoute = (Spinner) findViewById(R.id.spnRoute);
    Object selectedItem = spnRoute.getSelectedItem();

    if (selectedItem == null) {
      return null;
    } else {
      String selectedItemText = selectedItem.toString().toLowerCase();
      if (selectedItemText.equals(IDmeAffiliationType.MILITARY.getKey().toLowerCase())) {
        return IDmeAffiliationType.MILITARY;
      } else if (selectedItemText.equals(IDmeAffiliationType.STUDENT.getKey().toLowerCase())) {
        return IDmeAffiliationType.STUDENT;
      } else if (selectedItemText.equals(IDmeAffiliationType.TEACHER.getKey().toLowerCase())) {
        return IDmeAffiliationType.TEACHER;
      } else if (selectedItemText.equals(IDmeAffiliationType.RESPONDER.getKey().toLowerCase())) {
        return IDmeAffiliationType.RESPONDER;
      } else if (selectedItemText.equals(IDmeAffiliationType.GOVERNMENT.getKey().toLowerCase())) {
        return IDmeAffiliationType.GOVERNMENT;
      }
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
