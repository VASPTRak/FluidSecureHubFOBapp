package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;
import com.google.gson.Gson;

public class AcceptOdoActivity extends AppCompatActivity {

    private static final String TAG = "AcceptOdoActivity :";
    private EditText editOdoTenths;
    private String vehicleNumber;
    private String odometerTenths;
    private ProgressBar progressBar;
    private ConnectionDetector cd;

    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    String PreviousOdo = "", OdoLimit = "", OdometerReasonabilityConditions = "", CheckOdometerReasonable = "";

    String TimeOutinMinute;
    boolean Istimeout_Sec = true;

   public int cnt123 = 0;

    @Override
    protected void onResume() {
        super.onResume();

        if (Constants.CurrentSelectedHose.equals("FS1")) {
            editOdoTenths.setText(ZR(String.valueOf(Constants.AccOdoMeter_FS1)));
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            editOdoTenths.setText(ZR(String.valueOf(Constants.AccOdoMeter)));
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            editOdoTenths.setText(ZR(String.valueOf(Constants.AccOdoMeter_FS3)));
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            editOdoTenths.setText(ZR(String.valueOf(Constants.AccOdoMeter_FS4)));
        }

        /*
        //Set/Reset EnterOdometer text
        if (Constants.CurrentSelectedHose.equals("FS1")) {
            if (Constants.AccOdoMeter_FS1 != 0) {
                editOdoTenths.setText(String.valueOf(Constants.AccOdoMeter_FS1));
            } else {
                editOdoTenths.setText(String.valueOf(""));
            }

        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            if (Constants.AccOdoMeter != 0) {
                editOdoTenths.setText(String.valueOf(Constants.AccOdoMeter));
            } else {
                editOdoTenths.setText(String.valueOf(""));
            }
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            if (Constants.AccOdoMeter_FS3 != 0) {
                editOdoTenths.setText(String.valueOf(Constants.AccOdoMeter_FS3));
            } else {
                editOdoTenths.setText(String.valueOf(""));
            }
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            if (Constants.AccOdoMeter_FS4 != 0) {
                editOdoTenths.setText(String.valueOf(Constants.AccOdoMeter_FS4));
            } else {
                editOdoTenths.setText(String.valueOf(""));
            }
        }
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHandler.addActivities(2, AcceptOdoActivity.this);

        setContentView(R.layout.activity_accept_odo);

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        InItGUI();

        SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

        PreviousOdo = sharedPrefODO.getString("PreviousOdo", "");
        OdoLimit = sharedPrefODO.getString("OdoLimit", "");
        OdometerReasonabilityConditions = sharedPrefODO.getString("OdometerReasonabilityConditions", "");
        CheckOdometerReasonable = sharedPrefODO.getString("CheckOdometerReasonable", "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec) {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(AcceptOdoActivity.this);
                    Intent intent = new Intent(AcceptOdoActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        }, screenTimeOut);

        vehicleNumber = getIntent().getStringExtra(Constants.VEHICLE_NUMBER);


        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
            if (Constants.AccOdoMeter_FS1 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS1 + "");
            }

        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
            if (Constants.AccOdoMeter > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter + "");
            }

        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
            if (Constants.AccOdoMeter_FS3 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS3 + "");
            }

        } else {
            if (Constants.AccOdoMeter_FS4 > 0) {
                editOdoTenths.setText(Constants.AccOdoMeter_FS4 + "");
            }
        }


    }

    public String ZR(String zeroString) {
        if (zeroString.trim().equalsIgnoreCase("0"))
            return "";
        else
            return zeroString;

    }


    private void InItGUI() {
        try {
            editOdoTenths = (EditText) findViewById(R.id.editOdoTenths);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void cancelAction(View v) {

        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveButtonAction(View view) {
        try {
            Istimeout_Sec = false;

            if (!editOdoTenths.getText().toString().trim().isEmpty()) {

                int C_AccOdoMeter;
                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                    Constants.AccOdoMeter_FS1 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS1;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")) {
                    Constants.AccOdoMeter = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter;
                } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")) {
                    Constants.AccOdoMeter_FS3 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS3;
                } else {
                    Constants.AccOdoMeter_FS4 = Integer.parseInt(editOdoTenths.getText().toString().trim());
                    C_AccOdoMeter = Constants.AccOdoMeter_FS4;
                }

                AppConstants.WriteinFile("AcceptOdoActivity~~~~~~~~~" + Constants.AccOdoMeter);
                //allValid();

                int PO = Integer.parseInt(PreviousOdo.trim());
                int OL = Integer.parseInt(OdoLimit.trim());

                if (CheckOdometerReasonable.trim().toLowerCase().equalsIgnoreCase("true")) {

                    if (OdometerReasonabilityConditions.trim().equalsIgnoreCase("1")) {

                        if (C_AccOdoMeter >= PO && C_AccOdoMeter <= OL) {
                            //gooooo
                            allValid();
                        } else {
                            cnt123 += 1;

                            if (cnt123 > 3) {
                                //gooooo
                                allValid();
                            } else {
                                editOdoTenths.setText("");
                                AppConstants.colorToastBigFont(getApplicationContext(), "Bad odometer! Please try again.", Color.RED);
                            }
                        }

                    } else {

                        if (C_AccOdoMeter >= PO && C_AccOdoMeter <= OL) {
                            ///gooooo
                            allValid();
                        } else {
                            editOdoTenths.setText("");
                            AppConstants.colorToastBigFont(getApplicationContext(), "Bad odometer! Please try again.", Color.RED);
                        }
                    }
                } else {

                    //comment By JB -it  must take ANY number they enter on the 4th try
                    allValid();


                }


            } else {
                CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Error Message", "Please enter odometer, and try again.");
            }

            /*
            if (!editOdoTenths.getText().toString().isEmpty()) {


                progressBar.setVisibility(View.VISIBLE);


                AuthEntityClass authEntityClass = CommonUtils.getWiFiDetails(AcceptOdoActivity.this, AppConstants.LAST_CONNECTED_SSID);


                odometerTenths = editOdoTenths.getText().toString();


                authEntityClass.OdoMeter = Integer.valueOf(odometerTenths);
                authEntityClass.VehicleNumber = vehicleNumber;
                authEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptOdoActivity.this);
                authEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
                authEntityClass.SiteId = Integer.parseInt(AcceptVehicleActivity.SITE_ID);

                cd = new ConnectionDetector(AcceptOdoActivity.this);
                if (cd.isConnectingToInternet()) {


                    AuthTestAsynTask authTestAsynTask = new AuthTestAsynTask(authEntityClass);
                    authTestAsynTask.execute();
                    authTestAsynTask.get();

                    String serverRes = authTestAsynTask.response;

                    if (serverRes != null) {
                        progressBar.setVisibility(View.GONE);

                        JSONObject jsonObject = new JSONObject(serverRes);

                        String ResponceMessage = jsonObject.getString("ResponceMessage");


                        if (ResponceMessage.equalsIgnoreCase("success")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            String VehicleId = jsonObjectRD.getString("VehicleId");
                            String PhoneNumber = jsonObjectRD.getString("PhoneNumber");
                            String PersonId = jsonObjectRD.getString("PersonId");
                            String PulseRatio = jsonObjectRD.getString("PulseRatio");
                            String MinLimit = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate = jsonObjectRD.getString("ServerDate");
                            String IntervalToStopFuel = jsonObjectRD.getString("PulserStopTime");
                            System.out.println("iiiiii"+IntervalToStopFuel);

                            CommonUtils.SaveVehiFuelInPref(AcceptOdoActivity.this, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate,IntervalToStopFuel);

                            odometerTenths = editOdoTenths.getText().toString();
                            Intent intent = new Intent(this, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                            intent.putExtra(Constants.ODO_METER, odometerTenths);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                            String ResponceText = jsonObject.getString("ResponceText");
                            CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Message", ResponceText);
                        }

                    } else {
                        CommonUtils.showNoInternetDialog(AcceptOdoActivity.this);
                    }
                } else
                    AppConstants.colorToast(AcceptOdoActivity.this, "Please check Internet Connection.", Color.RED);


            } else {
                CommonUtils.showMessageDilaog(AcceptOdoActivity.this, "Error Message", "Please enter odometer, and try again.");
            }*/

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void allValid() {


        SharedPreferences sharedPrefODO = AcceptOdoActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
        String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
        String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");


       if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

            Intent i = new Intent(AcceptOdoActivity.this, AcceptPinActivity.class);//AcceptPinActivity
            startActivity(i);

        }else  if (IsHoursRequire.equalsIgnoreCase("True")) {

            Intent i = new Intent(AcceptOdoActivity.this, AcceptHoursAcitvity.class);
            startActivity(i);

        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {


            Intent i = new Intent(AcceptOdoActivity.this, AcceptDeptActivity.class);
            startActivity(i);

        } else if (IsOtherRequire.equalsIgnoreCase("True")) {

            Intent i = new Intent(AcceptOdoActivity.this, AcceptOtherActivity.class);
            startActivity(i);

        } else {

            AcceptServiceCall asc = new AcceptServiceCall();
            asc.activity = AcceptOdoActivity.this;
            asc.checkAllFields();
        }


    }


    public class AuthTestAsynTask extends AsyncTask<Void, Void, Void> {

        AuthEntityClass authEntityClass = null;

        public String response = null;

        public AuthTestAsynTask(AuthEntityClass authEntityClass) {
            this.authEntityClass = authEntityClass;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(authEntityClass);
                String userEmail = CommonUtils.getCustomerDetails(AcceptOdoActivity.this).Email;


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntityClass.IMEIUDID + ":" + userEmail + ":" + "AuthorizationSequence");
                response = serverHandler.PostTextData(AcceptOdoActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }

    @Override
    public void onBackPressed() {
        Istimeout_Sec = false;
        finish();
    }


}
