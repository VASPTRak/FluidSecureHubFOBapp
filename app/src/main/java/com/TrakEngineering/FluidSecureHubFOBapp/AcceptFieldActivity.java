package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;
import com.google.gson.Gson;

import org.json.JSONObject;

public class AcceptFieldActivity extends AppCompatActivity {

    LinearLayout linearOdo, linearDept, linearPerso, linearOther;
    EditText etOdometer, etDeptNumber, etPersonnelPin, etOther;
    Button btnSave, btnCancel;

    private String vehicleNumber;

    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";

    private ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_field);
        linearDept = (LinearLayout) findViewById(R.id.linearDept);
        linearPerso = (LinearLayout) findViewById(R.id.linearPerso);
        linearOther = (LinearLayout) findViewById(R.id.linearOther);
        linearOdo = (LinearLayout) findViewById(R.id.linearOdo);
        etOdometer = (EditText) findViewById(R.id.etOdometer);
        etDeptNumber = (EditText) findViewById(R.id.etDeptNumber);
        etPersonnelPin = (EditText) findViewById(R.id.etPersonnelPin);
        etOther = (EditText) findViewById(R.id.etOther);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);


        linearOdo.setVisibility(View.GONE);
        linearDept.setVisibility(View.GONE);
        linearPerso.setVisibility(View.GONE);
        linearOther.setVisibility(View.GONE);

        vehicleNumber = getIntent().getStringExtra(Constants.VEHICLE_NUMBER);


        SharedPreferences sharedPrefODO = AcceptFieldActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");


        if (IsOdoMeterRequire.equalsIgnoreCase("True")) {
            linearOdo.setVisibility(View.VISIBLE);
        } else {
            linearOdo.setVisibility(View.GONE);
        }

        if (IsDepartmentRequire.equalsIgnoreCase("True")) {
            linearDept.setVisibility(View.VISIBLE);
        } else {
            linearDept.setVisibility(View.GONE);
        }

        if (IsPersonnelPINRequire.equalsIgnoreCase("True")) {
            linearPerso.setVisibility(View.VISIBLE);
        } else {
            linearPerso.setVisibility(View.GONE);
        }

        if (IsOtherRequire.equalsIgnoreCase("True")) {
            linearOther.setVisibility(View.VISIBLE);
        } else {
            linearOther.setVisibility(View.GONE);
        }


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {

                    boolean odo=true,dept=true,pin=true,oth=true;

                    if (IsOdoMeterRequire.equalsIgnoreCase("True")) {

                        if (etOdometer.getText().toString().trim().isEmpty()) {

                            odo=false;

                            CommonUtils.showMessageDilaog(AcceptFieldActivity.this, "Error Message", "Please enter odometer.");
                        }

                    } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {

                        if (etDeptNumber.getText().toString().trim().isEmpty()) {

                            dept=false;

                            CommonUtils.showMessageDilaog(AcceptFieldActivity.this, "Error Message", "Please enter Department Number.");
                        }

                    } else if (IsPersonnelPINRequire.equalsIgnoreCase("True")) {

                        if (etPersonnelPin.getText().toString().trim().isEmpty()) {

                            pin=false;

                            CommonUtils.showMessageDilaog(AcceptFieldActivity.this, "Error Message", "Please enter Personnel Pin.");
                        }

                    } else if (IsOtherRequire.equalsIgnoreCase("True")) {

                        if (etOther.getText().toString().trim().isEmpty()) {

                            oth=false;

                            CommonUtils.showMessageDilaog(AcceptFieldActivity.this, "Error Message", "Please enter Other.");
                        }
                    }


                    if (odo && dept && pin && oth) {

                        int odoval=0;
                        if(!etOdometer.getText().toString().trim().isEmpty())
                        {
                            odoval=Integer.valueOf(etOdometer.getText().toString().trim());
                        }

                        AuthEntityClass authEntityClass = new AuthEntityClass();

                        authEntityClass.VehicleNumber = vehicleNumber;
                        authEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptFieldActivity.this);
                        authEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
                        authEntityClass.SiteId = Integer.parseInt(AcceptVehicleActivity.SITE_ID);

                        authEntityClass.OdoMeter = odoval;
                        authEntityClass.DepartmentNumber = etDeptNumber.getText().toString().trim();
                        authEntityClass.PersonnelPIN = etPersonnelPin.getText().toString().trim();
                        authEntityClass.Other = etOther.getText().toString().trim();

                        cd = new ConnectionDetector(AcceptFieldActivity.this);
                        if (cd.isConnectingToInternet()) {


                            AuthTestAsynTask authTestAsynTask = new AuthTestAsynTask(authEntityClass);
                            authTestAsynTask.execute();
                            authTestAsynTask.get();

                            String serverRes = authTestAsynTask.response;

                            if (serverRes != null) {


                                JSONObject jsonObject = new JSONObject(serverRes);

                                String ResponceMessage = jsonObject.getString("ResponceMessage");


                                if (ResponceMessage.equalsIgnoreCase("success")) {


                                    if (Constants.CurrentSelectedHose.equals("FS1")) {

                                        String ResponceData = jsonObject.getString("ResponceData");

                                        JSONObject jsonObjectRD = new JSONObject(ResponceData);

                                        String TransactionId_FS1 = jsonObjectRD.getString("TransactionId_FS1");
                                        String VehicleId_FS1 = jsonObjectRD.getString("VehicleId");
                                        String PhoneNumber_FS1 = jsonObjectRD.getString("PhoneNumber");
                                        String PersonId_FS1 = jsonObjectRD.getString("PersonId");
                                        String PulseRatio_FS1 = jsonObjectRD.getString("PulseRatio");
                                        String MinLimit_FS1 = jsonObjectRD.getString("MinLimit");
                                        String FuelTypeId_FS1 = jsonObjectRD.getString("FuelTypeId");
                                        String ServerDate_FS1 = jsonObjectRD.getString("ServerDate");
                                        String IntervalToStopFuel_FS1 = jsonObjectRD.getString("PulserStopTime");
                                        String PrintDate_FS1 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS1);

                                        String Company_FS1 = jsonObjectRD.getString("Company");
                                        String Location_FS1 = jsonObjectRD.getString("Location");
                                        String PersonName_FS1 = jsonObjectRD.getString("PersonName");
                                        String BluetoothCardReader_FS1 = jsonObjectRD.getString("BluetoothCardReader");
                                        String PrinterName_FS1 = jsonObjectRD.getString("PrinterName");
                                        System.out.println("iiiiii" + IntervalToStopFuel_FS1);
                                        String vehicleNumber="";
                                        String accOther="";

                                        CommonUtils.SaveVehiFuelInPref_FS1(AcceptFieldActivity.this, TransactionId_FS1,VehicleId_FS1, PhoneNumber_FS1, PersonId_FS1, PulseRatio_FS1, MinLimit_FS1, FuelTypeId_FS1, ServerDate_FS1, IntervalToStopFuel_FS1,PrintDate_FS1,Company_FS1,Location_FS1,PersonName_FS1,BluetoothCardReader_FS1,PrinterName_FS1,vehicleNumber,accOther);


                                        Intent intent = new Intent(AcceptFieldActivity.this, DisplayMeterActivity.class);
                                        intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                                        intent.putExtra(Constants.ODO_METER, etOdometer.getText().toString().trim());
                                        intent.putExtra(Constants.DEPT, etDeptNumber.getText().toString().trim());
                                        intent.putExtra(Constants.PPIN, etPersonnelPin.getText().toString().trim());
                                        intent.putExtra(Constants.OTHERR, etOther.getText().toString().trim());

                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);


                                    }else{

                                    String ResponceData = jsonObject.getString("ResponceData");

                                    JSONObject jsonObjectRD = new JSONObject(ResponceData);

                                    String TransactionId = jsonObjectRD.getString("TransactionId");
                                    String VehicleId = jsonObjectRD.getString("VehicleId");
                                    String PhoneNumber = jsonObjectRD.getString("PhoneNumber");
                                    String PersonId = jsonObjectRD.getString("PersonId");
                                    String PulseRatio = jsonObjectRD.getString("PulseRatio");
                                    String MinLimit = jsonObjectRD.getString("MinLimit");
                                    String FuelTypeId = jsonObjectRD.getString("FuelTypeId");
                                    String ServerDate = jsonObjectRD.getString("ServerDate");
                                    String IntervalToStopFuel = jsonObjectRD.getString("PulserStopTime");
                                    String PrintDate = CommonUtils.getTodaysDateInStringPrint(ServerDate);
                                    String Company = jsonObjectRD.getString("Company");
                                    String Location = jsonObjectRD.getString("Location");
                                    String PersonName = jsonObjectRD.getString("PersonName");
                                    String BluetoothCardReader = jsonObjectRD.getString("BluetoothCardReader");
                                    String PrinterName = jsonObjectRD.getString("PrinterName");
                                    System.out.println("iiiiii" + IntervalToStopFuel);
                                    String vehicleNumber="";
                                    String accOther="";

                                    CommonUtils.SaveVehiFuelInPref(AcceptFieldActivity.this, TransactionId,VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel,PrintDate,Company,Location,PersonName,BluetoothCardReader,PrinterName,vehicleNumber,accOther);


                                    Intent intent = new Intent(AcceptFieldActivity.this, DisplayMeterActivity.class);
                                    intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                                    intent.putExtra(Constants.ODO_METER, etOdometer.getText().toString().trim());
                                    intent.putExtra(Constants.DEPT, etDeptNumber.getText().toString().trim());
                                    intent.putExtra(Constants.PPIN, etPersonnelPin.getText().toString().trim());
                                    intent.putExtra(Constants.OTHERR, etOther.getText().toString().trim());

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                    }

                                } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                                    String ResponceText = jsonObject.getString("ResponceText");
                                    CommonUtils.showMessageDilaog(AcceptFieldActivity.this, "Message", ResponceText);
                                }

                            } else {
                                CommonUtils.showNoInternetDialog(AcceptFieldActivity.this);
                            }
                        } else
                            AppConstants.colorToast(AcceptFieldActivity.this, "Please check Internet Connection.", Color.RED);


                    }

                } catch (Exception ex) {
                    Log.e("", ex.getMessage());
                }

            }
        });

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
                String userEmail = CommonUtils.getCustomerDetails(AcceptFieldActivity.this).Email;


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntityClass.IMEIUDID + ":" + userEmail + ":" + "AuthorizationSequence");
                response = serverHandler.PostTextData(AcceptFieldActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("", "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }

}
