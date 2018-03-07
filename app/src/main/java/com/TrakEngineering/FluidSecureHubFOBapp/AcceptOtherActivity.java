package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AcceptOtherActivity extends AppCompatActivity {

    TextView tv_otherlabel;
    EditText etOther;
    Button btnSave, btnCancel;//AppConstants.OtherLabel

    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "",OtherLabel = "";
    String TimeOutinMinute;
    boolean Istimeout_Sec=true;

    @Override
    protected void onResume() {
        super.onResume();

        //Set/Reset EnterOther text
        if (Constants.CurrentSelectedHose.equals("FS1")) {
            etOther.setText(Constants.AccOther_FS1);
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            etOther.setText(Constants.AccOther_FS1);
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            etOther.setText(Constants.AccOther_FS3);
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            etOther.setText(Constants.AccOther_FS4);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHandler.addActivities(5, AcceptOtherActivity.this);

        setContentView(R.layout.activity_accept_other);

        etOther = (EditText) findViewById(R.id.etOther);
        tv_otherlabel = (TextView) findViewById(R.id.tv_otherlabel);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {
            if (Constants.AccOther != null) {
                etOther.setText(Constants.AccOther_FS1);
            }

        } else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")){

            if (Constants.AccOther != null) {
                etOther.setText(Constants.AccOther);
            }
        }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")){

            if (Constants.AccOther_FS3 != null) {
                etOther.setText(Constants.AccOther_FS3);
            }
        }else {

            if (Constants.AccOther_FS4 != null) {
                etOther.setText(Constants.AccOther_FS4);
            }
        }

        SharedPreferences sharedPrefODO = AcceptOtherActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        OtherLabel = sharedPrefODO.getString(AppConstants.OtherLabel, "Other");

        tv_otherlabel.setText(OtherLabel);
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");


        long screenTimeOut= Integer.parseInt(TimeOutinMinute) *60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec)
                {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(AcceptOtherActivity.this);
                    Intent intent = new Intent(AcceptOtherActivity.this,WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        }, screenTimeOut);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Istimeout_Sec=false;

                if (!etOther.getText().toString().trim().isEmpty()) {

                    if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1")) {

                        Constants.AccOther_FS1 = etOther.getText().toString().trim();
                    }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")){
                        Constants.AccOther = etOther.getText().toString().trim();
                    }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")){
                        Constants.AccOther_FS3 = etOther.getText().toString().trim();
                    }else{
                        Constants.AccOther_FS4 = etOther.getText().toString().trim();
                    }

                    AcceptServiceCall asc = new AcceptServiceCall();
                    asc.activity = AcceptOtherActivity.this;
                    asc.checkAllFields();

                } else {
                    CommonUtils.showMessageDilaog(AcceptOtherActivity.this, "Error Message", "Please enter Other, and try again.");
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Istimeout_Sec=false;
        finish();
    }
}
