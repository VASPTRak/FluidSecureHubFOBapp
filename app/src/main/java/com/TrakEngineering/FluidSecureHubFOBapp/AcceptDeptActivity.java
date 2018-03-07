package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AcceptDeptActivity extends AppCompatActivity {

    EditText   etDeptNumber;
    TextView tv_return, tv_swipekeybord;
    Button btnSave, btnCancel;
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    String TimeOutinMinute;
    boolean Istimeout_Sec=true;
    RelativeLayout footer_keybord;

    @Override
    protected void onResume() {
        super.onResume();
        //Set/Reset EnterPin text
        if (Constants.CurrentSelectedHose.equals("FS1")) {
            etDeptNumber.setText(Constants.AccDepartmentNumber_FS1);
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            etDeptNumber.setText(Constants.AccDepartmentNumber);
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            etDeptNumber.setText(Constants.AccDepartmentNumber_FS3);
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            etDeptNumber.setText(Constants.AccDepartmentNumber_FS4);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityHandler.addActivities(3,AcceptDeptActivity.this);

        setContentView(R.layout.activity_accept_dept);
        etDeptNumber = (EditText) findViewById(R.id.etDeptNumber);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);


        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        etDeptNumber.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean ps = isKeyboardShown(etDeptNumber.getRootView());
                if (ps == true) {
                    footer_keybord.setEnabled(true);
                    footer_keybord.setVisibility(View.VISIBLE);
                } else {
                    footer_keybord.setEnabled(false);
                    footer_keybord.setVisibility(View.INVISIBLE);
                }

            }
        });

        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1"))
        {
            if(Constants.AccDepartmentNumber_FS1!=null)
            {
                etDeptNumber.setText(Constants.AccDepartmentNumber_FS1);
            }

        }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")){
            if(Constants.AccDepartmentNumber!=null)
            {
                etDeptNumber.setText(Constants.AccDepartmentNumber);
            }
        }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")){
            if(Constants.AccDepartmentNumber_FS3!=null)
            {
                etDeptNumber.setText(Constants.AccDepartmentNumber_FS3);
            }
        }else{
            if(Constants.AccDepartmentNumber_FS4!=null)
            {
                etDeptNumber.setText(Constants.AccDepartmentNumber_FS4);
            }
        }



        SharedPreferences sharedPrefODO = AcceptDeptActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        long screenTimeOut= Integer.parseInt(TimeOutinMinute) *60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec)
                {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(AcceptDeptActivity.this);
                    Intent intent = new Intent(AcceptDeptActivity.this,WelcomeActivity.class);
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

                if (!etDeptNumber.getText().toString().trim().isEmpty()) {

                    if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1"))
                    {
                        Constants.AccDepartmentNumber_FS1 =  etDeptNumber.getText().toString().trim();
                    }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")){
                        Constants.AccDepartmentNumber =  etDeptNumber.getText().toString().trim();
                    }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")){
                        Constants.AccDepartmentNumber_FS3 =  etDeptNumber.getText().toString().trim();
                    }else{
                        Constants.AccDepartmentNumber_FS4 =  etDeptNumber.getText().toString().trim();
                    }

                    if (IsOtherRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptDeptActivity.this, AcceptOtherActivity.class);
                            startActivity(intent);

                    } else {

                        AcceptServiceCall asc = new AcceptServiceCall();
                        asc.activity = AcceptDeptActivity.this;
                        asc.checkAllFields();
                    }

                    /*
                   if (IsOtherRequire.equalsIgnoreCase("True")) {
                        Intent intent = new Intent(AcceptDeptActivity.this, AcceptOtherActivity.class);
                        startActivity(intent);
                    } else {

                        AcceptServiceCall asc = new AcceptServiceCall();
                        asc.activity = AcceptDeptActivity.this;
                        asc.checkAllFields();
                    }*/
                } else {
                    CommonUtils.showMessageDilaog(AcceptDeptActivity.this, "Error Message", "Please enter Department Number, and try again.");
                }

            }
        });

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = etDeptNumber.getInputType();
                if (InputTyp == 3) {
                    etDeptNumber.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for 123");
                } else {

                    etDeptNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for ABC");
                }

            }
        });

        tv_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeybord();
            }
        });
    }

    //============SoftKeyboard enable/disable Detection======
    private boolean isKeyboardShown(View rootView) {
    /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
    /* heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
        int heightDiff = rootView.getBottom() - r.bottom;
    /* Threshold size: dp to pixels, multiply with display density */
        boolean isKeyboardShown = heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;

        Log.d("TAG", "isKeyboardShown ? " + isKeyboardShown + ", heightDiff:" + heightDiff + ", density:" + dm.density
                + "root view height:" + rootView.getHeight() + ", rect:" + r);

        return isKeyboardShown;
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

    public void hideKeybord() {

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}
