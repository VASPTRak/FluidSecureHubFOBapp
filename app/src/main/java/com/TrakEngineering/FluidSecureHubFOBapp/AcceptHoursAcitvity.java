package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;


public class AcceptHoursAcitvity extends AppCompatActivity {

    private static final String TAG = "AcceptHoursAcitvity :";
    private EditText etHours;
    private String vehicleNumber;
    private String odometerTenths;
    private ProgressBar progressBar;
    private ConnectionDetector cd;

    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "", IsHoursRequire = "";
    String TimeOutinMinute;
    boolean Istimeout_Sec=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_accept_hours_acitvity);
        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        InItGUI();

        SharedPreferences sharedPrefODO = AcceptHoursAcitvity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        long screenTimeOut= Integer.parseInt(TimeOutinMinute) *60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec)
                {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(AcceptHoursAcitvity.this);
                    Intent intent = new Intent(AcceptHoursAcitvity.this,WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        }, screenTimeOut);

        vehicleNumber = getIntent().getStringExtra(Constants.VEHICLE_NUMBER);


    }

    private void InItGUI() {
        try {
            etHours = (EditText) findViewById(R.id.etHours);
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

            Istimeout_Sec=false;



            if (!etHours.getText().toString().trim().isEmpty()) {

                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1"))
                    Constants.AccHours_FS1 = Integer.parseInt(etHours.getText().toString().trim());

                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2"))
                    Constants.AccHours = Integer.parseInt(etHours.getText().toString().trim());

                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3"))
                    Constants.AccHours_FS3 = Integer.parseInt(etHours.getText().toString().trim());

                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS4"))
                    Constants.AccHours_FS4 = Integer.parseInt(etHours.getText().toString().trim());


                SharedPreferences sharedPrefODO = AcceptHoursAcitvity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                String IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
                String IsHoursRequire = sharedPrefODO.getString(AppConstants.IsHoursRequire, "");
                String IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
                String IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

                if (IsDepartmentRequire.equalsIgnoreCase("True")) {

                        Intent intent = new Intent(AcceptHoursAcitvity.this, AcceptDeptActivity.class);
                        startActivity(intent);

                } else if (IsOtherRequire.equalsIgnoreCase("True")) {

                        Intent intent = new Intent(AcceptHoursAcitvity.this, AcceptOtherActivity.class);
                        startActivity(intent);

                } else {

                    AcceptServiceCall asc = new AcceptServiceCall();
                    asc.activity = AcceptHoursAcitvity.this;
                    asc.checkAllFields();
                }


                 /*
                if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
                     Constants.AccHours_FS1 = Integer.parseInt(etHours.getText().toString().trim());

                    if (Constants.AccHours_FS1 > 0) {
                        if (IsOdoMeterRequire.equalsIgnoreCase("True")) {
                            Intent intent = new Intent(this, AcceptOdoActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                            startActivity(intent);
                        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {
                            Intent intent = new Intent(this, AcceptDeptActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                            startActivity(intent);
                        } else if (IsOtherRequire.equalsIgnoreCase("True")) {
                            Intent intent = new Intent(this, AcceptOtherActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                            startActivity(intent);
                        } else {

                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptHoursAcitvity.this;
                            asc.checkAllFields();
                        }
                    } else {
                        CommonUtils.showMessageDilaog(AcceptHoursAcitvity.this, "Error Message", "Please enter Hours greater than 0");
                    }
                }else {
                    Constants.AccHours = Integer.parseInt(etHours.getText().toString().trim());

                    if (Constants.AccHours > 0) {
                        if (IsOdoMeterRequire.equalsIgnoreCase("True")) {
                            Intent intent = new Intent(this, AcceptOdoActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                            startActivity(intent);
                        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {
                            Intent intent = new Intent(this, AcceptDeptActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                            startActivity(intent);
                        } else if (IsOtherRequire.equalsIgnoreCase("True")) {
                            Intent intent = new Intent(this, AcceptOtherActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, vehicleNumber);
                            startActivity(intent);
                        } else {

                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptHoursAcitvity.this;
                            asc.checkAllFields();
                        }
                    } else {
                        CommonUtils.showMessageDilaog(AcceptHoursAcitvity.this, "Error Message", "Please enter Hours greater than 0");
                    }

                }
*/

            } else {
                CommonUtils.showMessageDilaog(AcceptHoursAcitvity.this, "Error Message", "Please enter Hours");
            }



        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }
    @Override
    public void onBackPressed() {
        Istimeout_Sec=false;
        finish();
    }


}
