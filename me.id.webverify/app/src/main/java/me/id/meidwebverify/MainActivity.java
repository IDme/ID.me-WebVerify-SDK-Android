package me.id.meidwebverify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import me.id.webverifylib.IDmeWebVerify;

public class MainActivity extends ActionBarActivity {
  private IDmeWebVerify webVerify;
  private String clientID = null;
  private String redirectUri = null;
  private boolean returnProperties = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
    Spinner spnRoute = (Spinner) findViewById(R.id.spnRoute);
    Spinner propRoute = (Spinner) findViewById(R.id.spnProperties);
    String affiliationType = "";

    returnProperties = propRoute.getSelectedItem().toString().equals("Yes");

    Object selectedItem = spnRoute.getSelectedItem();
    if (selectedItem == null) {
      Toast.makeText(this, "Affiliation Type is required", Toast.LENGTH_LONG).show();
      return;
    } else if (selectedItem.toString().equals("Military")) {
      affiliationType = IDmeWebVerify.MILITARY;
    } else if (selectedItem.toString().equals("Student")) {
      affiliationType = IDmeWebVerify.STUDENT;
    } else if (selectedItem.toString().equals("Teacher")) {
      affiliationType = IDmeWebVerify.TEACHER;
    } else if (selectedItem.toString().equals("First Responder")) {
      affiliationType = IDmeWebVerify.FIRST_RESPONDER;
    } else if (selectedItem.toString().equals("Government")) {
      affiliationType = IDmeWebVerify.GOVERNMENT;
    }

    webVerify = new IDmeWebVerify(clientID, redirectUri, affiliationType, this, returnProperties);
    webVerify.StartWebView();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == IDmeWebVerify.WEB_REQUEST_CODE) {
        String response = data.getStringExtra(IDmeWebVerify.IDME_WEB_VERIFY_RESPONSE);

        TextView txtResult = (TextView) findViewById(R.id.txtResult);
        txtResult.setText("Response : " + response);
      }
    }
  }
}
