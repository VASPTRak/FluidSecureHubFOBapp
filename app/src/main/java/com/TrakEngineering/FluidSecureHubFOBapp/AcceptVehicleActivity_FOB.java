package com.TrakEngineering.FluidSecureHubFOBapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.VehicleFobEntity;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AcceptVehicleActivity_FOB extends AppCompatActivity {

    private static final String TAG = "AcceptVehicleActivitFOB";
    public static String SITE_ID = "0";
    private String IsOdoMeterRequire = "";
    private String IsDepartmentRequire = "";
    private String IsPersonnelPINRequire = "";
    private String IsPersonnelPINRequireForHub = "";
    private String IsOtherRequire = "";
    private Button btnCancel;
    private Button btn_fob_Reader;
    private Button btnSave;
    private RelativeLayout footer_keybord;
    private LinearLayout Linear_layout_Save_back_buttons;
    private TextView tv_return;
    private TextView tv_swipekeybord;
    private TextView tv_fob_number;
    private TextView tv_vehicle_no_below;
    private TextView tv_dont_have_fob;
    private TextView tv_enter_vehicle_no;
    private LinearLayout Linear_layout_vehicleNumber;
    private String TimeOutinMinute;
    private boolean Istimeout_Sec = true;
    private long screenTimeOut;
    private Timer t;
    private Timer ScreenOutTimeVehicle;
    private boolean started_process = false;
    private EditText editVehicleNumber;

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;

    @Override
    protected void onResume() {
        super.onResume();
        try {

            //Set up the adaptors
            if (mAdapter != null) {
                mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
                mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
            }

            editVehicleNumber.setText(Constants.AccVehicleNumber);

            DisplayScreenInit();
            Istimeout_Sec = true;
            TimeoutVehicleScreen();


            t = new Timer();
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    //do something
                    System.out.println("Vehi FOK_KEY" + AppConstants.APDU_FOB_KEY);
                    if (!AppConstants.APDU_FOB_KEY.equalsIgnoreCase("") && AppConstants.APDU_FOB_KEY.length() > 6 && !started_process) {
                        started_process = true;
                        try {


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    editVehicleNumber.setText("");
                                    Istimeout_Sec = false;
                                    ScreenOutTimeVehicle.cancel();
                                    GetVehicleNuOnFobKeyDetection();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            CallSaveButtonFunctionality();//Press Enter fun
                                        }
                                    }, 2000);

                                }
                            });

                            t.cancel();
                        } catch (Exception e) {

                            System.out.println(e);
                            e.printStackTrace();
                        }
                    }

                }

            };
            t.schedule(tt, 500, 500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        AppConstants.APDU_FOB_KEY = "";
        t.cancel();//Stop timer FOB Key
        ScreenOutTimeVehicle.cancel();//Stop screen out timer
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter != null) {

            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                    "Message from NFC Reader :-)", Locale.ENGLISH, true)});
        }

        ActivityHandler.addActivities(1, AcceptVehicleActivity_FOB.this);
        setContentView(R.layout.activity_accept_vehicle_fob);

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        InItGUI();

        SharedPreferences sharedPrefODO = AcceptVehicleActivity_FOB.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        AppConstants.HUB_ID = sharedPrefODO.getString(AppConstants.HubId, "");


        SharedPreferences sharedPref = AcceptVehicleActivity_FOB.this.getSharedPreferences(Constants.PREF_COLUMN_SITE, Context.MODE_PRIVATE);
        String dataSite = sharedPref.getString(Constants.PREF_COLUMN_SITE, "");

        SITE_ID = parseSiteData(dataSite);

        //Check Selected FS and  change accordingly
        //Constants.AccVehicleNumber = "";
        //Constants.AccOdoMeter = 0;
        //Constants.AccHours = 0;
        //Constants.AccDepartmentNumber = "";
        //Constants.AccPersonnelPIN = "";
        //Constants.AccOther = "";

        editVehicleNumber.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                boolean ps = isKeyboardShown(editVehicleNumber.getRootView());
//                if (ps == true) {
//                    footer_keybord.setEnabled(true);
//                    footer_keybord.setVisibility(View.VISIBLE);
//                } else {
                    footer_keybord.setEnabled(false);
                    footer_keybord.setVisibility(View.INVISIBLE);
//                }

            }
        });

        tv_swipekeybord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int InputTyp = editVehicleNumber.getInputType();
                if (InputTyp == 3) {
                    editVehicleNumber.setInputType(InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for 123");
                } else {

                    editVehicleNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
                    tv_swipekeybord.setText("Press for ABC");
                }

            }
        });

        tv_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        btn_fob_Reader.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {

               /* editVehicleNumber.setText("");
                Istimeout_Sec = false;
                FobBtnDisable();//Disable Use fob key.
                //Fob-Reader code..
                AppConstants.FOB_KEY_VEHICLE = "";
                Readfobkey();*/

            }
        });
        editVehicleNumber.setEnabled(false);
        editVehicleNumber.setVisibility(View.GONE);
        tv_dont_have_fob.setVisibility(View.GONE);
        btnSave.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        tv_swipekeybord.setEnabled(false);
        tv_swipekeybord.setVisibility(View.GONE);
        hideKeyboard();
    }


    private String parseSiteData(String dataSite) {
        String ssiteId = "";
        try {
            if (dataSite != null) {
                JSONArray jsonArray = new JSONArray(dataSite);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);

                    String SiteId = jo.getString("SiteId");
                    String Password = jo.getString("Password");

                    System.out.println("Wifi Password...." + Password);

                    //AppConstants.WIFI_PASSWORD = "";

                    ssiteId = SiteId;
                }
            }
        } catch (Exception ex) {

            CommonUtils.LogMessage(TAG, "", ex);
            ex.printStackTrace();
        }

        return ssiteId;
    }

    private void InItGUI() {

        Linear_layout_vehicleNumber = (LinearLayout) findViewById(R.id.Linear_layout_vehicleNumber);
        btn_fob_Reader = (Button) findViewById(R.id.btn_fob_Reader);
        btnSave = (Button) findViewById(R.id.btnSave);
        footer_keybord = (RelativeLayout) findViewById(R.id.footer_keybord);
        Linear_layout_Save_back_buttons = (LinearLayout) findViewById(R.id.Linear_layout_Save_back_buttons);
        tv_return = (TextView) findViewById(R.id.tv_return);
        tv_swipekeybord = (TextView) findViewById(R.id.tv_swipekeybord);
        tv_fob_number = (TextView) findViewById(R.id.tv_fob_number);
        tv_vehicle_no_below = (TextView) findViewById(R.id.tv_vehicle_no_below);
        tv_enter_vehicle_no = (TextView) findViewById(R.id.tv_enter_vehicle_no);
        tv_dont_have_fob = (TextView) findViewById(R.id.tv_dont_have_fob);

        try {
            btnCancel = (Button) findViewById(R.id.btnCancel);
            editVehicleNumber = (EditText) findViewById(R.id.editVehicleNumber);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public void cancelAction(View v) {

        hideKeyboard();
        onBackPressed();
    }

    public void saveButtonAction(View v) {
        CallSaveButtonFunctionality();
    }

    private void CallSaveButtonFunctionality() {


        try {


            String V_Number = editVehicleNumber.getText().toString().trim();


            if (!V_Number.isEmpty() || !AppConstants.APDU_FOB_KEY.isEmpty()) {


                String vehicleNumber = "";
                String pinNumber = "";

                    pinNumber = Constants.AccPersonnelPIN;
                    vehicleNumber = editVehicleNumber.getText().toString().trim();
                    Constants.AccVehicleNumber = vehicleNumber;


                VehicleFobEntity objEntityClass = new VehicleFobEntity();
                objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptVehicleActivity_FOB.this);
                objEntityClass.VehicleNumber = vehicleNumber;
                objEntityClass.FOBNumber = AppConstants.APDU_FOB_KEY;
                AppConstants.FOB_KEY_VEHICLE = AppConstants.APDU_FOB_KEY;

                CheckVehicleFobOnly vehTestAsynTask = new CheckVehicleFobOnly(objEntityClass);
                vehTestAsynTask.execute();
                vehTestAsynTask.get();

                String serverRes = vehTestAsynTask.response;

                if (serverRes != null) {


                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");

                    System.out.println("ResponceMessage.." + ResponceMessage);


                    if (ResponceMessage.equalsIgnoreCase("success")) {

                       /* SharedPreferences sharedPrefODO = AcceptVehicleActivity_FOB.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

                        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
                        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
                        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
                        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");

                        IsOdoMeterRequire = jsonObject.getString("IsOdoMeterRequire");
                        String IsHoursRequire = jsonObject.getString("IsHoursRequire");
                        String PreviousOdo = jsonObject.getString("PreviousOdo");
                        String OdoLimit = jsonObject.getString("OdoLimit");
                        String OdometerReasonabilityConditions = jsonObject.getString("OdometerReasonabilityConditions");
                        String CheckOdometerReasonable = jsonObject.getString("CheckOdometerReasonable");


                        SharedPreferences sharedPref = AcceptVehicleActivity_FOB.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
                        editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
                        editor.putString("PreviousOdo", PreviousOdo);
                        editor.putString("OdoLimit", OdoLimit);
                        editor.putString("OdometerReasonabilityConditions", OdometerReasonabilityConditions);
                        editor.putString("CheckOdometerReasonable", CheckOdometerReasonable);
                        editor.commit();*/

                        Istimeout_Sec = false;
                        AppConstants.APDU_FOB_KEY = "";
                        finish();

/*
                        if (IsOdoMeterRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_FOB.this, AcceptOdoActivity.class);//AcceptPinActivity
                            startActivity(intent);

                        }else if (IsPersonnelPINRequireForHub.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_FOB.this, AcceptPinActivity.class);//AcceptPinActivity
                            startActivity(intent);

                        }else  if (IsHoursRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_FOB.this, AcceptHoursAcitvity.class);
                            startActivity(intent);

                        } else if (IsDepartmentRequire.equalsIgnoreCase("True")) {


                            Intent intent = new Intent(AcceptVehicleActivity_FOB.this, AcceptDeptActivity.class);
                            startActivity(intent);

                        } else if (IsOtherRequire.equalsIgnoreCase("True")) {

                            Intent intent = new Intent(AcceptVehicleActivity_FOB.this, AcceptOtherActivity.class);
                            startActivity(intent);

                        } else {

                            AcceptServiceCall asc = new AcceptServiceCall();
                            asc.activity = AcceptVehicleActivity_FOB.this;
                            asc.checkAllFields();
                        //}*/

                    } else {
                        String ResponceText = jsonObject.getString("ResponceText");
//                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");
//                        if (ValidationFailFor.equalsIgnoreCase("Pin")) {
//                            AppConstants.colorToastBigFont(this, ResponceText, Color.RED);
//                            AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_FOB.this);
//                            Istimeout_Sec = false;
//                            AppConstants.APDU_FOB_KEY = "";
//                            finish();
//
//                        } else {
                            //Empty Fob key & enable edit text and Enter button
                            // AppConstants.FOB_KEY_VEHICLE = "";
                            editVehicleNumber.setEnabled(true);
                            btnSave.setEnabled(true);
                            tv_vehicle_no_below.setText("Enter Vehicle Number:");
                        CommonUtils.showCustomMessageDilaog(AcceptVehicleActivity_FOB.this, "Message", ResponceText);
//                        }

                    }

                } else {
                    //Empty Fob key & enable edit text and Enter button
                    AppConstants.APDU_FOB_KEY = "";
                    editVehicleNumber.setEnabled(true);
                    btnSave.setEnabled(true);
                    CommonUtils.showNoInternetDialog(AcceptVehicleActivity_FOB.this);
                }



            } else {
                //Empty Fob key & enable edit text and Enter button
                AppConstants.APDU_FOB_KEY = "";
                editVehicleNumber.setEnabled(true);
                btnSave.setEnabled(true);
                CommonUtils.showMessageDilaog(AcceptVehicleActivity_FOB.this, "Error Message", "Please enter vehicle number or use fob key.");
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void GetVehicleNuOnFobKeyDetection() {

        try {

            String vehicleNumber = "";
            String pinNumber = "";

                pinNumber = Constants.AccPersonnelPIN;
                vehicleNumber = editVehicleNumber.getText().toString().trim();
                Constants.AccVehicleNumber = vehicleNumber;


            //VehicleRequireEntity objEntityClass = new VehicleRequireEntity();
            //SW: use once server changes
            VehicleFobEntity objEntityClass = new VehicleFobEntity();
            objEntityClass.IMEIUDID = AppConstants.getIMEI(AcceptVehicleActivity_FOB.this);
            objEntityClass.VehicleNumber = vehicleNumber;
            objEntityClass.FOBNumber = AppConstants.APDU_FOB_KEY;


            CheckVehicleFobOnly vehTestAsynTask = new CheckVehicleFobOnly(objEntityClass);
            vehTestAsynTask.execute();
            vehTestAsynTask.get();

            String serverRes = vehTestAsynTask.response;

            if (serverRes != null) {


                JSONObject jsonObject = new JSONObject(serverRes);

                String ResponceMessage = jsonObject.getString("ResponceMessage");
                System.out.println("ResponceMessage...." + ResponceMessage);


                if (ResponceMessage.equalsIgnoreCase("success")) {


                    IsOdoMeterRequire = jsonObject.getString("IsOdoMeterRequire");
                    String IsHoursRequire = jsonObject.getString("IsHoursRequire");
                    String VehicleNumber = jsonObject.getString("VehicleNumber");
                    String PreviousOdo = jsonObject.getString("PreviousOdo");
                    String OdoLimit = jsonObject.getString("OdoLimit");
                    String OdometerReasonabilityConditions = jsonObject.getString("OdometerReasonabilityConditions");
                    String CheckOdometerReasonable = jsonObject.getString("CheckOdometerReasonable");

                    SharedPreferences sharedPref = AcceptVehicleActivity_FOB.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
                    editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
                    editor.putString("PreviousOdo", PreviousOdo);
                    editor.putString("OdoLimit", OdoLimit);
                    editor.putString("OdometerReasonabilityConditions", OdometerReasonabilityConditions);
                    editor.putString("CheckOdometerReasonable", CheckOdometerReasonable);
                    editor.commit();

                    editVehicleNumber.setText(VehicleNumber);
                    tv_vehicle_no_below.setText("Vehicle Number: " + VehicleNumber);
                    tv_fob_number.setText("Fob No: " + AppConstants.APDU_FOB_KEY);


                    DisplayScreenFobReadSuccess();


                } else {
                    String ResponceText = jsonObject.getString("ResponceText");
//                    String ValidationFailFor = jsonObject.getString("ValidationFailFor");
//                    if (ValidationFailFor.equalsIgnoreCase("Pin")) {
//                        AppConstants.colorToastBigFont(this, ResponceText, Color.RED);
//                        AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_FOB.this);
//                        Istimeout_Sec = false;
//                        AppConstants.APDU_FOB_KEY = "";
//                        finish();
//
//                    } else {

                        Istimeout_Sec = true;
                        TimeoutVehicleScreen();
                        tv_enter_vehicle_no.setText("Invalid FOB or Unassigned FOB");
                        tv_enter_vehicle_no.setVisibility(View.VISIBLE);
                        tv_fob_number.setVisibility(View.GONE);
                        btn_fob_Reader.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        btnSave.setVisibility(View.VISIBLE);
                        tv_vehicle_no_below.setVisibility(View.GONE);
                        tv_dont_have_fob.setVisibility(View.VISIBLE);
                        tv_dont_have_fob.setText("Enter Vehicle Number below on keypad");
                        editVehicleNumber.setVisibility(View.VISIBLE);
                        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);
                    // CommonUtils.showMessageDilaog(AcceptVehicleActivity_FOB.this, "Message", ResponceText);
//                    }

                }

            } else {
                // CommonUtils.showNoInternetDialog(AcceptVehicleActivity_FOB.this);
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_FOB.this);
        Istimeout_Sec = false;
        AppConstants.APDU_FOB_KEY = "";
        finish();
    }

    private void TimeoutVehicleScreen() {

        SharedPreferences sharedPrefODO = AcceptVehicleActivity_FOB.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");

        screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        System.out.println("ScreenOutTimeVehicle" + screenTimeOut);

        ScreenOutTimeVehicle = new Timer();
        TimerTask tttt = new TimerTask() {
            @Override
            public void run() {
                //do something
                if (Istimeout_Sec) {

                    try {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideKeyboard();
                                Istimeout_Sec = false;
                                AppConstants.ClearEdittextFielsOnBack(AcceptVehicleActivity_FOB.this);
                                Intent intent = new Intent(AcceptVehicleActivity_FOB.this, WelcomeActivity_FOB.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        });

                        ScreenOutTimeVehicle.cancel();
                    } catch (Exception e) {

                        System.out.println(e);
                        e.printStackTrace();
                    }

                }

            }

        };
        ScreenOutTimeVehicle.schedule(tttt, screenTimeOut, 500);


    }

    private void DisplayScreenInit() {

        //showKeybord();
        //AppConstants.APDU_FOB_KEY = "";
        //editVehicleNumber.setText("");
        tv_enter_vehicle_no.setVisibility(View.GONE);
        tv_vehicle_no_below.setVisibility(View.GONE);
        tv_fob_number.setVisibility(View.GONE);
        editVehicleNumber.setVisibility(View.VISIBLE);
        btn_fob_Reader.setVisibility(View.VISIBLE);
        tv_dont_have_fob.setVisibility(View.VISIBLE);
        Linear_layout_Save_back_buttons.setVisibility(View.VISIBLE);


        editVehicleNumber.setEnabled(false);
        editVehicleNumber.setVisibility(View.GONE);
        tv_dont_have_fob.setVisibility(View.GONE);
        btnSave.setEnabled(false);
        btnSave.setVisibility(View.GONE);
        tv_swipekeybord.setEnabled(false);
        tv_swipekeybord.setVisibility(View.GONE);
        hideKeyboard();
    }

    private void DisplayScreenFobReadSuccess() {

        tv_enter_vehicle_no.setVisibility(View.VISIBLE);
        tv_fob_number.setVisibility(View.VISIBLE);
        tv_vehicle_no_below.setVisibility(View.VISIBLE);
        tv_dont_have_fob.setVisibility(View.GONE);
        editVehicleNumber.setVisibility(View.GONE);
        Linear_layout_Save_back_buttons.setVisibility(View.GONE);
        btn_fob_Reader.setVisibility(View.GONE);

    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ex) {
            System.out.println("tried to hide the keyboard, but already hidden");
            ex.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConstants.APDU_FOB_KEY = "";
        //required
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
            mAdapter.disableForegroundNdefPush(this);
        }
    }

    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    @Override
    public void onNewIntent(Intent intent) {
//    String uri = intent.getDataString();
        String action = intent.getAction();
        System.out.println("saw intent");
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            System.out.println("saw nfc");
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] id = tag.getId();
            String FobKey = toReversedHex(id);
            System.out.println("read nfc " + FobKey);
            if (FobKey.length() > 6) {
                AppConstants.APDU_FOB_KEY = FobKey + " 90 00 ";
            }
        }
    }

    public class CheckVehicleFobOnly extends AsyncTask<Void, Void, Void> {

        public String response = null;
        //VehicleRequireEntity vrentity = null;

        //SW: I will use this once server does
        VehicleFobEntity vfentity = null;

        public CheckVehicleFobOnly(VehicleFobEntity vfentity) {
            this.vfentity = vfentity;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(vfentity);
                String userEmail = CommonUtils.getCustomerDetails(AcceptVehicleActivity_FOB.this).PersonEmail;

                System.out.println("jsonDatajsonDatajsonData" + jsonData);
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(vfentity.IMEIUDID + ":" + userEmail + ":" + "CheckVehicleFobOnly");
                response = serverHandler.PostTextData(AcceptVehicleActivity_FOB.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

                //SW: Use once server changes
//                //----------------------------------------------------------------------------------
//                String authString = "Basic " + AppConstants.convertStingToBase64(vrentity.IMEIUDID + ":" + userEmail + ":" + "CheckVehicleFobOnly");
//                response = serverHandler.PostTextData(AcceptVehicleActivity_FOB.this, AppConstants.webURL, jsonData, authString);
//                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "CheckVehicleFobOnly ", ex);
                ex.printStackTrace();
            }
            return null;
        }

    }
}