package com.TrakEngineering.FluidSecureHubFOBapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    EditText etFName, etMobile, etCompany;
    AutoCompleteTextView etEmail;
    Button btnSubmit;
    private Pattern EMAIL_PATTERN;
    private Pattern US_PHONE_PATTERN;

    public static void redToast(Context ctx, String MSg) {
        Toast toast = Toast.makeText(ctx, " " + MSg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(Color.RED);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // ----------------------------------------------------------------------------------------------
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // ----------------------------------------------------------------------------------------------

        getSupportActionBar().setTitle("New HUB Registration");

        etFName = (EditText) findViewById(R.id.etFName);
        etMobile = (EditText) findViewById(R.id.etMobile);
        etCompany = (EditText) findViewById(R.id.etCompany);
        etEmail = (AutoCompleteTextView) findViewById(R.id.etEmail);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(RegistrationActivity.this));

        US_PHONE_PATTERN = Pattern.compile("^(?:(?:\\+?1\\s*(?:[.-]\\s*)?)?(?:\\(\\s*([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9])\\s*\\)|([2-9]1[02-9]|[2-9][02-8]1|[2-9][02-8][02-9]))\\s*(?:[.-]\\s*)?)?([2-9]1[02-9]|[2-9][02-9]1|[2-9][02-9]{2})\\s*(?:[.-]\\s*)?([0-9]{4})(?:\\s*(?:#|x\\.?|ext\\.?|extension)\\s*(\\d+))?$", Pattern.CASE_INSENSITIVE);

        EMAIL_PATTERN = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
        Account[] accounts = AccountManager.get(this).getAccounts();
        Set<String> emailSet = new HashSet<String>();
        for (Account account : accounts) {
            if (EMAIL_PATTERN.matcher(account.name).matches()) {
                emailSet.add(account.name);
            }
        }
        etEmail.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(emailSet)));






        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (etFName.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Name");
                    etFName.requestFocus();
                }/* else if (etMobile.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Mobile");
                    etMobile.requestFocus();
                } else if (!US_PHONE_PATTERN.matcher(etMobile.getText().toString().trim()).matches()) {

                    redToast(RegistrationActivity.this, "Please enter valid US contact number in\n(xxx)-xxx-xxxx or xxx-xxx-xxxx format.");
                    etMobile.requestFocus();
                } else if (!etMobile.getText().toString().trim().contains("-")) {
                    redToast(RegistrationActivity.this, "Please enter valid US contact number in \n(xxx)-xxx-xxxx or xxx-xxx-xxxx format.");
                    etMobile.requestFocus();

                } else if (etEmail.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Email");
                    etEmail.requestFocus();
                } else if (!isValidEmail(etEmail.getText().toString().trim())) {
                    redToast(RegistrationActivity.this, "Invalid Email");
                    etEmail.requestFocus();
                } else if (etCompany.getText().toString().trim().isEmpty()) {
                    redToast(RegistrationActivity.this, "Please enter Company");
                    etCompany.requestFocus();
                }*/ else {

                    //------------Collect information for Registration------------------------------
                    //------------------------------------------------------------------------------
                    storeINFO(RegistrationActivity.this, etFName.getText().toString().trim(), etMobile.getText().toString().trim(), etEmail.getText().toString().trim(), AppConstants.getIMEI(RegistrationActivity.this));

                    String userName = etFName.getText().toString().trim();
                    String userMobile = etMobile.getText().toString().trim();
                    String userEmail = etEmail.getText().toString().trim();
                    String userCompany = etCompany.getText().toString().trim();
                    String imeiNumber = AppConstants.getIMEI(RegistrationActivity.this);

                    new RegisterUser(userName, userMobile, userEmail, imeiNumber, AppConstants.DEVICE_TYPE, userCompany).execute();

                    //------------------------------------------------------------------------------


                }
            }
        });
    }

    public void storeINFO(Context context, String name, String mobile, String email, String IMEInum) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeINFO", 0);
        editor = pref.edit();


        // Storing
        editor.putString("name", name);
        editor.putString("mobile", mobile);
        editor.putString("email", email);
        editor.putString("IMEInum", IMEInum);

        editor.commit();


    }

    public void AlertDialogBox(final Context ctx, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {


                        dialog.dismiss();

                        finish();
                    }
                }

        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public class RegisterUser extends AsyncTask<Void, Void, String> {

        private static final String TAG = "RegisterUser :";
        ProgressDialog pd;
        String userName;
        String userMobile;
        String userEmail;
        String imeiNumber;
        String deviceType;
        String userCompany;

        RegisterUser(String userName, String userMobile, String userEmail, String imeiNumber, String deviceType, String userCompany) {
            this.userName = userName;
            this.userMobile = userMobile;
            this.userEmail = userEmail;
            this.imeiNumber = imeiNumber;
            this.deviceType = deviceType;
            this.userCompany = userCompany;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(RegistrationActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

               // String sendData = userName + "#:#" + userMobile + "#:#" + userEmail + "#:#" + imeiNumber + "#:#" + deviceType + "#:#" + userCompany + "#:#" + "AP";
                String sendData = userName + "#:#" + "" + "#:#" + "" + "#:#" + imeiNumber + "#:#" + deviceType + "#:#" + "" + "#:#" + "AP";

                String AUTH_TOKEN = "Basic " + AppConstants.convertStingToBase64("123:abc:Register");
                ServerHandler serverHandler = new ServerHandler();

                resp = serverHandler.PostTextData(RegistrationActivity.this, AppConstants.webURL, sendData, AUTH_TOKEN);


            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }

            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            try {

                CommonUtils.LogMessage(TAG, " RegisterUser :" + result, new Exception("Test for All"));

                JSONObject jsonObj = new JSONObject(result);

                String ResponceMessage = jsonObj.getString(AppConstants.RES_MESSAGE);

                if (ResponceMessage.equalsIgnoreCase("success")) {
                    CommonUtils.SaveUserInPref(RegistrationActivity.this, userName, userMobile, userEmail, "","","","","", "", "","","");

                    AlertDialogBox(RegistrationActivity.this, "Thank you for registering. \n\nYour request has been sent for the approval. You will be able to proceed with the application after your request has been approved by the administrator.");
                } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                    String ResponseText = jsonObj.getString(AppConstants.RES_TEXT);

                    AppConstants.AlertDialogBox(RegistrationActivity.this, ResponseText);

                } else if (ResponceMessage.equalsIgnoreCase("exists")) {
                    AlertDialogBox(RegistrationActivity.this, "Your IMEI Number already EXISTS!");
                } else {
                    AlertDialogBox(RegistrationActivity.this, "Network Error");
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " RegisterUser :" + result, e);
            }

        }
    }



}
