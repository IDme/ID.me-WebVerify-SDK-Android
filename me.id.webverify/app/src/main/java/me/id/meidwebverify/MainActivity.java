package me.id.meidwebverify;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import me.id.webverifylib.IDmeAffiliation;
import me.id.webverifylib.IDmeAffiliationType;
import me.id.webverifylib.IDmeConnectionType;
import me.id.webverifylib.IDmeProfile;
import me.id.webverifylib.IDmeWebVerify;
import me.id.webverifylib.listener.IDmeCompletableListener;
import me.id.webverifylib.listener.IDmeGetAccessTokenListener;
import me.id.webverifylib.listener.IDmeGetProfileListener;
import me.id.webverifylib.listener.IDmeScope;

public class MainActivity extends AppCompatActivity {
  private String clientId = null;
  private String secretId = null;
  private String redirectUri = null;
  private boolean returnProperties = true;
  private TextView txtResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    IDmeWebVerify.initialize(this, clientId, secretId, redirectUri);

    setContentView(R.layout.activity_main);

    txtResult = (TextView) findViewById(R.id.txtResult);

    Button btnLogin = (Button) findViewById(R.id.btnLogin);
    btnLogin.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        login();
      }
    });

    Button btnAddAffiliation = findViewById(R.id.btnAddAffiliation);
    btnAddAffiliation.setOnClickListener(v -> addAffiliation());

    Button btnAddConnection = findViewById(R.id.btnAddConnection);
    btnAddConnection.setOnClickListener(v -> addConnection());
  }

  /**
   * Method that starts de authentication process
   */
  void login() {
    Spinner propRoute = findViewById(R.id.spnProperties);
    returnProperties = propRoute.getSelectedItem().toString().equals("Yes");

    IDmeWebVerify.getInstance().login(this, Scope.DEFAULT, new IDmeGetAccessTokenListener() {
      @Override
      public void onSuccess(String accessToken) {
        if (returnProperties) {
          showUserProfileInformation(Scope.DEFAULT);
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
  void addAffiliation() {
    final IDmeAffiliationType affiliationType = getSelectedAffiliationType();
    if (affiliationType == null) {
      showError(new IllegalStateException("Affiliation Type is required"));
      return;
    }
    IDmeWebVerify.getInstance().registerAffiliation(this, Scope.DEFAULT, affiliationType, new IDmeCompletableListener() {
      @Override
      public void onSuccess() {
        Toast.makeText(MainActivity.this, String.format("Affiliation %s was correctly added.", affiliationType.getKey()), Toast.LENGTH_LONG).show();
      }

      @Override
      public void onError(Throwable throwable) {
        showError(throwable);
      }
    });
  }

  /**
   * Method that starts the process of adding a new Connection type
   */
  void addConnection() {
    final IDmeConnectionType connectionType = getSelectedConnectionType();
    if (connectionType == null) {
      showError(new IllegalStateException("Connection Type is required"));
      return;
    }
    IDmeWebVerify.getInstance().registerConnection(this, Scope.DEFAULT, connectionType, new IDmeCompletableListener() {
      @Override
      public void onSuccess() {
        Toast.makeText(MainActivity.this, "Connection " + connectionType + " was correctly added", Toast.LENGTH_LONG).show();
      }

      @Override
      public void onError(Throwable throwable) {
        showError(throwable);
      }
    });
  }

  @Nullable
  private IDmeAffiliationType getSelectedAffiliationType() {
    Spinner spinner = (Spinner) findViewById(R.id.spnAffiliation);
    Object selectedItem = spinner.getSelectedItem();

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

  @Nullable
  private IDmeConnectionType getSelectedConnectionType() {
    Spinner spinner = (Spinner) findViewById(R.id.spnConnection);
    Object selectedItem = spinner.getSelectedItem();

    if (selectedItem == null) {
      return null;
    } else {
      String selectedItemText = selectedItem.toString().toLowerCase();
      if (selectedItemText.equals(IDmeConnectionType.FACEBOOK.getKey().toLowerCase())) {
        return IDmeConnectionType.FACEBOOK;
      } else if (selectedItemText.equals(IDmeConnectionType.GOOGLE_PLUS.getKey().toLowerCase())) {
        return IDmeConnectionType.GOOGLE_PLUS;
      } else if (selectedItemText.equals(IDmeConnectionType.LINEDIN.getKey().toLowerCase())) {
        return IDmeConnectionType.LINEDIN;
      } else if (selectedItemText.equals(IDmeConnectionType.PAYPAL.getKey().toLowerCase())) {
        return IDmeConnectionType.PAYPAL;
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

  private void showUserProfileInformation(IDmeScope scope) {
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
