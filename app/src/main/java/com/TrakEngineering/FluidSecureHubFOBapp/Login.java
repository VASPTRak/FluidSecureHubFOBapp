package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

public class Login extends AppCompatActivity {

    private EditText etUserId;
    private EditText etPass;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUserId = (EditText) findViewById(R.id.etUserId);
        etPass = (EditText) findViewById(R.id.etPass);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        etUserId.setText(AppConstants.Login_Email);
        etPass.requestFocus();
        //etUserId.setEnabled(false);
        //etPass.setText("Fuel@123");


        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(Login.this));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (etUserId.getText().toString().trim().isEmpty()) {
                    etUserId.requestFocus();
                    AppConstants.AlertDialogBox(Login.this, "Please enter Username");
                } else if (etPass.getText().toString().trim().isEmpty()) {
                    etPass.requestFocus();
                    AppConstants.AlertDialogBox(Login.this, "Please enter Password");
                } else {

                    ConnectionDetector cd = new ConnectionDetector(Login.this);
                    if (cd.isConnectingToInternet())
                        new LoginTask().execute(etUserId.getText().toString().trim(), etPass.getText().toString().trim());
                    else
                        CommonUtils.showNoInternetDialog(Login.this);
                }
            }
        });
    }


    public class LoginTask extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(Login.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {


            try {


                MediaType TEXT = MediaType.parse("application/x-www-form-urlencoded");

                OkHttpClient client = new OkHttpClient();
                String imieNumber = AppConstants.getIMEI(Login.this);
                RequestBody body = RequestBody.create(TEXT, "Authenticate");
                Request request = new Request.Builder()
                        .url(AppConstants.LoginURL)
                        .post(body)
                        .addHeader("Login", "Basic " + AppConstants.convertStingToBase64(imieNumber + ":" + param[0] + ":" + param[1]))
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            System.out.println("login resp......." + result);


            pd.dismiss();
            try {


                JSONObject jsonObj = new JSONObject(result);

                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);

                if (ResponceMessage.equalsIgnoreCase("success")) {


                    if (CommonUtils.isMobileDataEnabled(Login.this)) {
                        System.out.println("MobileDataEnabled.....");
                    } else {
                        System.out.println("MobileDataOffff.....");
                    }


                    if (CommonUtils.isWiFiEnabled(Login.this)) {
                        System.out.println("WiFiWiFiEnabled.....");
                    } else {
                        System.out.println("WiFiOffff.....");
                    }

                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Login.this, WelcomeActivity.class));
                    finish();

                } else {
                    String ResponseText = jsonObj.getString(AppConstants.RES_TEXT);

                    AppConstants.AlertDialogBox(Login.this, ResponseText);

                }


            } catch (Exception e) {

                CommonUtils.LogMessage("TAG", " RegisterUser :" + result, e);
            }

        }
    }
}
