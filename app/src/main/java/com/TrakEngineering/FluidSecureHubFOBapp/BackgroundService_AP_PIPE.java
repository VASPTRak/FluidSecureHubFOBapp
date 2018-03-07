package com.TrakEngineering.FluidSecureHubFOBapp;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.RenameHose;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.TrazComp;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.UpdateTransactionStatusClass;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by VASP on 7/24/2017.
 */

public class BackgroundService_AP_PIPE extends BackgroundService {


    String EMPTY_Val = "";
    private ConnectionDetector cd;

    //String HTTP_URL = "http://192.168.43.140:80/";//for pipe
    //String HTTP_URL = "http://192.168.43.5:80/";//Other FS
    String HTTP_URL = "";

    String URL_INFO = HTTP_URL + "client?command=info";
    String URL_STATUS = HTTP_URL + "client?command=status";
    String URL_RECORD = HTTP_URL + "client?command=record10";

    String URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
    String URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";

    String URL_WIFI = HTTP_URL + "config?command=wifi";
    String URL_RELAY = HTTP_URL + "config?command=relay";

    String URL_GET_USER = HTTP_URL + "upgrade?command=getuser";
    String URL_RESET = HTTP_URL + "upgrade?command=reset";
    String URL_FILE_UPLOAD = HTTP_URL + "upgrade?command=start";

    String URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
    String URL_SET_TXNID = HTTP_URL + "config?command=txtnid";

    String jsonRename;
    String jsonConnectWifi = "{\"Request\":  {\"Station\":{\"Connect_Station\":{\"ssid\":\"tenda\",\"password\":\"1234567890\",\"token\":\"1234567890123456789012345678901234567890\"}}}}";
    String jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

    String jsonPulsar = "{\"pulsar_request\":{\"counter_set\":1}}";
    String jsonPulsarOff = "{\"pulsar_request\":{\"counter_set\":0}}";

    ArrayList<HashMap<String, String>> quantityRecords = new ArrayList<>();

    SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private String vehicleNumber, odometerTenths = "0", dNumber = "", pNumber = "", oText = "", hNumber = "";
    String  LinkName,OtherName,IsOtherRequire,OtherLabel,VehicleNumber,PrintDate,CompanyName,Location,PersonName,PrinterMacAddress,PrinterName,TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel;

    int timeFirst = 60;
    Timer tFirst;
    TimerTask taskFirst;
    boolean stopTimer = true;
    boolean pulsarConnected = false;
    double minFuelLimit = 0, numPulseRatio = 0;
    String consoleString = "", outputQuantity = "0";
    double CurrentLat = 0, CurrentLng = 0;
    GoogleApiClient mGoogleApiClient;
    long stopAutoFuelSeconds = 0;
    boolean isTransactionComp = false;
    double fillqty = 0;
    Integer Pulses = 0;
    long sqliteID = 0;
    ConnectivityManager connection_manager;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("Service", "null");
                this.stopSelf();
                Constants.FS_1STATUS = "FREE";
                if (!Constants.BusyVehicleNumberList.equals(null)) {
                    Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
                }


            } else {
                Log.d("Service", "not null");
                HTTP_URL = (String) extras.get("HTTP_URL");

                URL_INFO = HTTP_URL + "client?command=info";
                URL_STATUS = HTTP_URL + "client?command=status";
                URL_RECORD = HTTP_URL + "client?command=record10";

                URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
                URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";

                URL_WIFI = HTTP_URL + "config?command=wifi";
                URL_RELAY = HTTP_URL + "config?command=relay";

                URL_GET_USER = HTTP_URL + "upgrade?command=getuser";
                URL_RESET = HTTP_URL + "upgrade?command=reset";
                URL_FILE_UPLOAD = HTTP_URL + "upgrade?command=start";

                URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
                URL_SET_TXNID = HTTP_URL + "config?command=txtnid";


                jsonConnectWifi = "{\"Request\":  {\"Station\":{\"Connect_Station\":{\"ssid\":\"tenda\",\"password\":\"1234567890\",\"token\":\"1234567890123456789012345678901234567890\"}}}}";
                jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
                jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

                jsonPulsar = "{\"pulsar_request\":{\"counter_set\":1}}";
                jsonPulsarOff = "{\"pulsar_request\":{\"counter_set\":0}}";

                jsonRename = "{\"Request\":{\"SoftAP\":{\"Connect_SoftAP\":{\"authmode\":\"WPAPSK/WPA2PSK\",\"channel\":6,\"ssid\":\"" + AppConstants.REPLACEBLE_WIFI_NAME_FS1 + "\",\"password\":\"123456789\"}}}}";


                System.out.println("BackgroundService is on. AP_FS_PIPE" + HTTP_URL);
                Constants.FS_1STATUS = "BUSY";
                Constants.BusyVehicleNumberList.add(Constants.AccVehicleNumber_FS1);

                SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
                TransactionId = sharedPref.getString("TransactionId_FS1", "");
                VehicleId = sharedPref.getString("VehicleId_FS1", "");
                PhoneNumber = sharedPref.getString("PhoneNumber_FS1", "");
                PersonId = sharedPref.getString("PersonId_FS1", "");
                PulseRatio = sharedPref.getString("PulseRatio_FS1", "1");
                MinLimit = sharedPref.getString("MinLimit_FS1", "0");
                FuelTypeId = sharedPref.getString("FuelTypeId_FS1", "");
                ServerDate = sharedPref.getString("ServerDate_FS1", "");
                IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS1", "0");

                LinkName = AppConstants.CURRENT_SELECTED_SSID;

                //settransactionID to FSUNIT
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        new CommandsPOST().execute(URL_SET_TXNID, "{\"txtnid\":" + TransactionId + "}");

                    }
                }, 1500);


                //Create and Empty transactiin into SQLite DB
                HashMap<String, String> mapsts = new HashMap<>();
                mapsts.put("transId", TransactionId);
                mapsts.put("transStatus", "1");

               controller.insertTransStatus(mapsts);
                ////////////////////////////////////////////
                String userEmail = CommonUtils.getCustomerDetails_backgroundService_PIPE(BackgroundService_AP_PIPE.this).Email;
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_AP_PIPE.this) + ":" + userEmail + ":" + "TransactionComplete");

                HashMap<String, String> imap = new HashMap<>();
                imap.put("jsonData", "");
                imap.put("authString", authString);

                sqliteID = controller.insertTransactions(imap);

                //////////////////////////////////////////////////////////////

                //=====================UpgradeTransaction Status ==1================
                cd = new ConnectionDetector(BackgroundService_AP_PIPE.this);
                if (cd.isConnectingToInternet()) {
                    try {
                        UpdateTransactionStatusClass authEntity = new UpdateTransactionStatusClass();
                        authEntity.TransactionId = TransactionId;
                        authEntity.Status = "1";
                        authEntity.IMEIUDID = AppConstants.getIMEI(BackgroundService_AP_PIPE.this);

                        BackgroundService_AP_PIPE.UpdateAsynTask authTestAsynTask = new BackgroundService_AP_PIPE.UpdateAsynTask(authEntity);
                        authTestAsynTask.execute();
                        authTestAsynTask.get();

                        String serverRes = authTestAsynTask.response;

                        if (serverRes != null) {
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {

                    AppConstants.colorToast(BackgroundService_AP_PIPE.this, "Please check Internet Connection.", Color.RED);
                    UpdateTransactionStatusClass authEntity = new UpdateTransactionStatusClass();
                    authEntity.TransactionId = TransactionId;
                    authEntity.Status = "1";
                    authEntity.IMEIUDID = AppConstants.getIMEI(BackgroundService_AP_PIPE.this);


                    Gson gson1 = new Gson();
                    String jsonData1 = gson1.toJson(authEntity);

                    System.out.println("AP_FS_PIPE UpdatetransactionData......" + jsonData1);

                    String userEmail1 = CommonUtils.getCustomerDetails_backgroundService_PIPE(this).PersonEmail;
                    String authString1 = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail1 + ":" + "UpgradeTransactionStatus");

                    HashMap<String, String> imapStatus = new HashMap<>();
                    imapStatus.put("jsonData", jsonData1);
                    imapStatus.put("authString", authString1);

                    controller.insertIntoUpdateTranStatus(imapStatus);

                }


                //=========================UpgradeTransactionStatus Ends===============

                minFuelLimit = Double.parseDouble(MinLimit);

                numPulseRatio = Double.parseDouble(PulseRatio);

                stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);


                System.out.println("iiiiii" + IntervalToStopFuel);
                System.out.println("minFuelLimit" + minFuelLimit);
                System.out.println("getDeviceName" + minFuelLimit);

                String mobDevName = AppConstants.getDeviceName().toLowerCase();
                System.out.println("oooooooooo" + mobDevName);


            }
        } catch (
                NullPointerException e)

        {
            Log.d("Ex", e.getMessage());
            this.stopSelf();
        }


        //GetLatLng();
        new

                CommandsPOST().

                execute(URL_RELAY, jsonRelayOn);

        new

                Handler().

                postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        new CommandsGET().execute(URL_RELAY);


                        new CommandsPOST().execute(URL_SET_PULSAR, jsonPulsarOff);

                    }
                }, 1000);

        new

                Handler().

                postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        new CommandsPOST().execute(URL_SET_PULSAR, jsonPulsar);

                    }
                }, 2500);

        new

                Handler().

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startQuantityInterval();
                    }
                }, 3000);

        // return super.onStartCommand(intent, flags, startId);
        return Service.START_NOT_STICKY;
    }


    @TargetApi(21)
    private void setGlobalWifiConnection() {

        NetworkRequest.Builder requestbuilder = new NetworkRequest.Builder();
        requestbuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        connection_manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);


        connection_manager.requestNetwork(requestbuilder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {


                System.out.println(" network......." + network);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connection_manager.bindProcessToNetwork(network);

                }
            }
        });
    }

    public void GetLatLng() {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            CurrentLat = mLastLocation.getLatitude();
            CurrentLng = mLastLocation.getLongitude();

            System.out.println("CCCrrr" + CurrentLat);
            System.out.println("CCCrrr" + CurrentLng);

        }
    }

    public void stopFirstTimer(boolean flag) {
        if (flag) {
            tFirst.cancel();
            tFirst.purge();
        } else {
            tFirst.cancel();
            tFirst.purge();

            WelcomeActivity.SelectedItemPos = -1;
            AppConstants.BUSY_STATUS = true;

            Intent i = new Intent(this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {


                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, param[1]);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
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


            try {

                System.out.println("APFS_PIPE OUTPUT" + result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class CommandsGET extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
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

            try {

                System.out.println("APFS_PIPE OUTPUT" + result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public void startQuantityInterval() {


        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                try {

                    if (stopTimer) {

                        /*
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                            setHttpTransportWifi(URL_GET_PULSAR, EMPTY_Val);

                        } else {

                        }
                        */

                        new BackgroundService_AP_PIPE.GETPulsarQuantity().execute(URL_GET_PULSAR);

                    }

                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        }, 0, 2000);


    }

    public class GETPulsarQuantity extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
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


            try {

                System.out.println("OUTPUT" + result);

                if (stopTimer)
                    pulsarQtyLogic(result);


            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public void pulsarQtyLogic(String result) {

        int secure_status = 0;

        try {
            if (result.contains("pulsar_status")) {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                String counts = joPulsarStat.getString("counts");
                String pulsar_status = joPulsarStat.getString("pulsar_status");
                String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");


                if (pulsar_status.trim().equalsIgnoreCase("1")) {
                    pulsarConnected = true;
                } else if (pulsar_status.trim().equalsIgnoreCase("0")) {

                    pulsarConnected = false;
                    if (!pulsarConnected) {

                        this.stopSelf();
                        Constants.FS_1STATUS = "FREE";
                        if (!Constants.BusyVehicleNumberList.equals(null)) {
                            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
                        }
                        System.out.println("APFS_PIPE Auto Stop! Pulsar disconnected");
                        // AppConstants.colorToastBigFont(this, AppConstants.FS1_CONNECTED_SSID+" Auto Stop!\n\nPulsar disconnected", Color.BLUE);
                        stopButtonFunctionality();
                    }
                }

                convertCountToQuantity(counts);


                if (!pulsar_secure_status.trim().isEmpty()) {
                    secure_status = Integer.parseInt(pulsar_secure_status);

                    if (secure_status == 0) {
                        //linearTimer.setVisibility(View.GONE);
                        //tvCountDownTimer.setText("-");

                    } else if (secure_status == 1) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("5");

                    } else if (secure_status == 2) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("4");

                    } else if (secure_status == 3) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("3");

                    } else if (secure_status == 4) {
                        //linearTimer.setVisibility(View.VISIBLE);
                        //tvCountDownTimer.setText("2");

                    } else if (secure_status >= 5) {
                        //linearTimer.setVisibility(View.GONE);
                        //tvCountDownTimer.setText("1");

                        this.stopSelf();
                        Constants.FS_1STATUS = "FREE";
                        if (!Constants.BusyVehicleNumberList.equals(null)) {
                            Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
                        }

                        System.out.println("APFS_PIPE Auto Stop! Count down timer completed");
                        AppConstants.colorToastBigFont(this, AppConstants.FS1_CONNECTED_SSID + " Auto Stop!\n\nCount down timer completed.", Color.BLUE);
                        stopButtonFunctionality();
                    }

                }

            }
            Date currDT = new Date();
            String strCurDT = sdformat.format(currDT);

            HashMap<String, String> hmap = new HashMap<>();
            hmap.put("a", outputQuantity);
            hmap.put("b", strCurDT);
            quantityRecords.add(hmap);

            //if quantity same for some interval
            secondsTimeLogic(strCurDT);


            //if quantity reach max limit
            if (!outputQuantity.trim().isEmpty()) {
                try {


                    if (minFuelLimit > 0) {
                        if (fillqty >= minFuelLimit) {

                            this.stopSelf();
                            Constants.FS_1STATUS = "FREE";
                            if (!Constants.BusyVehicleNumberList.equals(null)) {
                                Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
                            }
                            System.out.println("APFS_PIPE Auto Stop!You reached MAX fuel limit.");
                            //AppConstants.colorToastBigFont(this, "Auto Stop!\n\nYou reached MAX fuel limit.", Color.BLUE);
                            stopButtonFunctionality();
                        }
                    }
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void stopButtonFunctionality() {


        quantityRecords.clear();

        // btnStart.setVisibility(View.GONE);
        //btnStop.setVisibility(View.GONE);
        //btnFuelHistory.setVisibility(View.VISIBLE);
        consoleString = "";
        // tvConsole.setText("");

        //it stops pulsar logic------
        stopTimer = false;


        new BackgroundService_AP_PIPE.CommandsPOST().execute(URL_RELAY, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

                        String result = new BackgroundService_AP_PIPE.GETFINALPulsar().execute(URL_GET_PULSAR).get();


                        if (result.contains("pulsar_status")) {

                            JSONObject jsonObject = new JSONObject(result);
                            JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                            String counts = joPulsarStat.getString("counts");
                            //String pulsar_status = joPulsarStat.getString("pulsar_status");
                            //String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                            convertCountToQuantity(counts);

                            /*
                            if (i == 0)
                                cntA = counts;
                            else if (i == 1)
                                cntB = counts;
                            else
                                cntC = counts;
                            */


                            if (i == 2) {

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finalLastStep();
                                    }
                                }, 1000);


                            }


                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }, 1000);


    }

    public void finalLastStep() {


        new BackgroundService_AP_PIPE.CommandsPOST().execute(URL_SET_PULSAR, jsonPulsarOff);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (AppConstants.NeedToRenameFS1) {

                    consoleString += "RENAME:\n" + jsonRename;

                    new BackgroundService_AP_PIPE.CommandsPOST().execute(URL_WIFI, jsonRename);

                }
            }

        }, 2500);

        long secondsTime = 3000;

        if (AppConstants.NeedToRenameFS1) {
            secondsTime = 5000;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //AppConstants.disconnectWiFi(DisplayMeterActivity.this);
                GetDetails();
                TransactionCompleteFunction();

            }

        }, secondsTime);
    }

    public class GETFINALPulsar extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(param[0])
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


            try {

                consoleString += "OUTPUT- " + result + "\n";

                // tvConsole.setText(consoleString);

                System.out.println("APFS_PIPE OUTPUT" + result);


            } catch (Exception e) {

                System.out.println(e);
            }

        }

    }

    public void secondsTimeLogic(String currentDT) {

        try {


            if (quantityRecords.size() > 0) {

                Date nowDT = sdformat.parse(currentDT);
                Date d2 = sdformat.parse(quantityRecords.get(0).get("b"));

                long seconds = (nowDT.getTime() - d2.getTime()) / 1000;


                if (stopAutoFuelSeconds > 0) {

                    if (seconds >= stopAutoFuelSeconds) {

                        if (qtyFrequencyCount()) {

                            //qty is same for some time
                            System.out.println("APFS_PIPE Auto Stop!Quantity is same for last");
                            //AppConstants.colorToastBigFont(this, "Auto Stop!\n\nQuantity is same for last " + stopAutoFuelSeconds + " seconds.", Color.BLUE);
                            stopButtonFunctionality();
                            stopTimer = false;
                            this.stopSelf();
                            Constants.FS_1STATUS = "FREE";
                            if (!Constants.BusyVehicleNumberList.equals(null)) {
                                Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
                            }


                        } else {
                            quantityRecords.remove(0);
                            System.out.println("0 th pos deleted");
                            System.out.println("seconds--" + seconds);
                        }
                    }
                }

            }
        } catch (Exception e) {

        }
    }

    public boolean qtyFrequencyCount() {


        if (quantityRecords.size() > 0) {

            ArrayList<String> data = new ArrayList<>();

            for (HashMap<String, String> hm : quantityRecords) {
                data.add(hm.get("a"));
            }

            System.out.println("\n Count all with frequency");
            Set<String> uniqueSet = new HashSet<String>(data);

            System.out.println("size--" + uniqueSet.size());

            /*for (String temp : uniqueSet) {
                System.out.println(temp + ": " + Collections.frequency(data, temp));
            }*/

            if (uniqueSet.size() == 1) {  //Autostop unique records
                return true;
            }
        }

        return false;
    }

    public void convertCountToQuantity(String counts) {
        outputQuantity = counts;

        Pulses = Integer.parseInt(outputQuantity);
        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);

        DecimalFormat precision = new DecimalFormat("0.00");
        Constants.FS_1Gallons = (precision.format(fillqty));
        Constants.FS_1Pulse = outputQuantity;

        System.out.println("APFS_PIPE Pulse" + outputQuantity);
        System.out.println("APFS_PIPE Quantity" + (fillqty));

        ////////////////////////////////////-Update transaction ---
        TrazComp authEntityClass = new TrazComp();
        authEntityClass.TransactionId = TransactionId;
        authEntityClass.FuelQuantity = fillqty;
        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_AP_PIPE.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " ";
        authEntityClass.TransactionFrom = "A";
        authEntityClass.Pulses = Integer.parseInt(counts);

        Gson gson = new Gson();
        String jsonData = gson.toJson(authEntityClass);

        String userEmail = CommonUtils.getCustomerDetails_backgroundService_PIPE(BackgroundService_AP_PIPE.this).Email;
        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(BackgroundService_AP_PIPE.this) + ":" + userEmail + ":" + "TransactionComplete");


        HashMap<String, String> imap = new HashMap<>();
        imap.put("jsonData", jsonData);
        imap.put("authString", authString);
        imap.put("sqliteId", sqliteID + "");

        if (fillqty > 0) {
            int rowseffected = controller.updateTransactions(imap);
            System.out.println("rowseffected-" + rowseffected);


            controller.deleteTransStatusByTransID(TransactionId);
        }


    }

    public void GetDetails() {
        vehicleNumber = Constants.AccVehicleNumber_FS1;
        odometerTenths = Constants.AccOdoMeter_FS1 + "";
        dNumber = Constants.AccDepartmentNumber_FS1;
        pNumber = Constants.AccPersonnelPIN_FS1;
        oText = Constants.AccOther_FS1;
        hNumber = Constants.AccHours_FS1 + "";


        if (dNumber != null) {
        } else {
            dNumber = "";
        }

        if (pNumber != null) {
        } else {
            pNumber = "";
        }

        if (oText != null) {
        } else {
            oText = "";
        }
    }

    public void TransactionCompleteFunction() {

        SharedPreferences sharedPrefODO = this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        OtherLabel = sharedPrefODO.getString(AppConstants.OtherLabel, "Other");


        SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
        TransactionId = sharedPref.getString("TransactionId_FS1", "");
        VehicleId = sharedPref.getString("VehicleId_FS1", "");
        PhoneNumber = sharedPref.getString("PhoneNumber_FS1", "");
        PersonId = sharedPref.getString("PersonId_FS1", "");
        PulseRatio = sharedPref.getString("PulseRatio_FS1", "1");
        MinLimit = sharedPref.getString("MinLimit_FS1", "0");
        FuelTypeId = sharedPref.getString("FuelTypeId_FS1", "");
        ServerDate = sharedPref.getString("ServerDate_FS1", "");
        IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel_FS1", "0");

        PrintDate = sharedPref.getString("PrintDate_FS1", "");
        CompanyName = sharedPref.getString("Company_FS1", "");
        Location = sharedPref.getString("Location_FS1", "");
        PersonName = sharedPref.getString("PersonName_FS1", "");
        PrinterMacAddress = sharedPref.getString("PrinterMacAddress_FS1", "");
        PrinterName = sharedPref.getString("PrinterName_FS1", "");
        VehicleNumber = sharedPref.getString("vehicleNumber_FS1", "");
        OtherName = sharedPref.getString("accOther_FS1", "");


       /* String PD = CommonUtils.FormatPrintRecipte(sharedPref.getString("PrintDate_FS1", ""));
        System.out.println("printtttttttt  "+PD);*/

        //Print Transaction Receipt
        DecimalFormat precision = new DecimalFormat("0.00");
        String Qty = (precision.format(fillqty));



        String printReceipt;//32 char per line
        if (IsOtherRequire.equalsIgnoreCase("true")){
            printReceipt = " \n\n------FluidSecure Receipt------ \n\nCompany   : " + CompanyName +"\n\nTime/Date : "+PrintDate+"\n\nLocation  : "+LinkName+","+Location+","+"\n\nVehicle # : "+VehicleNumber+"\n\nPersonnel : "+PersonName+" \n\nQty       : " + Qty + "\n\n"+OtherLabel+":"+OtherName+ "\n\n ---------Thank You---------"+"\n\n\n\n\n\n\n\n\n\n\n\n";
        }else{
            printReceipt = " \n\n------FluidSecure Receipt------ \n\nCompany   : " + CompanyName +"\n\nTime/Date : "+PrintDate+"\n\nLocation  : "+LinkName+","+Location+"\n\nVehicle # : "+VehicleNumber+"\n\nPersonnel : "+PersonName+" \n\nQty       : " + Qty + "\n\n ---------Thank You---------"+"\n\n\n\n\n\n\n\n\n\n\n\n";
        }

        try {
            BluetoothPrinter.sendData(printReceipt);//printReceipt
        } catch (IOException e) {
            e.printStackTrace();
        }


   /*     //=====================UpgradeTransaction Status = 2=================

        UpdateTransactionStatusClass authEntity = new UpdateTransactionStatusClass();
        authEntity.TransactionId = TransactionId;
        authEntity.Status = "2";
        authEntity.IMEIUDID = AppConstants.getIMEI(BackgroundService_AP_PIPE.this);


        Gson gson1 = new Gson();
        String jsonData1 = gson1.toJson(authEntity);

        System.out.println("AP_FS_PIPE UpdatetransactionData......" + jsonData1);

        String userEmail1 = CommonUtils.getCustomerDetails_backgroundService_PIPE(this).PersonEmail;
        String authString1 = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail1 + ":" + "UpgradeTransactionStatus");

        HashMap<String, String> imapStatus = new HashMap<>();
        imapStatus.put("jsonData", jsonData1);
        imapStatus.put("authString", authString1);

        controller.insertIntoUpdateTranStatus(imapStatus);


        //=========================UpgradeTransactionStatus Ends===============*/

        try {

            TrazComp authEntityClass = new TrazComp();
            authEntityClass.TransactionId = TransactionId;
            authEntityClass.FuelQuantity = fillqty;
            authEntityClass.Pulses = Pulses;
            authEntityClass.TransactionFrom = "A";
            authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(BackgroundService_AP_PIPE.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " ";

            /*authEntityClass.PersonId = PersonId;
            authEntityClass.SiteId = AcceptVehicleActivity.SITE_ID;
            authEntityClass.VehicleId = VehicleId;
            authEntityClass.CurrentOdometer = odometerTenths;
            authEntityClass.FuelTypeId = FuelTypeId;
            authEntityClass.PhoneNumber = PhoneNumber;
            authEntityClass.WifiSSId = AppConstants.FS1_CONNECTED_SSID;//AppConstants.LAST_CONNECTED_SSID;
            authEntityClass.TransactionDate = ServerDate;
            authEntityClass.CurrentLat = "" + CurrentLat;
            authEntityClass.CurrentLng = "" + CurrentLng;
            authEntityClass.VehicleNumber = vehicleNumber;
            authEntityClass.DepartmentNumber = dNumber;
            authEntityClass.PersonnelPIN = pNumber;
            authEntityClass.Other = oText;
            authEntityClass.Hours = hNumber;*/

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            System.out.println("AP_FS_PIPE TrazComp......" + jsonData);

            String userEmail = CommonUtils.getCustomerDetails_backgroundService_PIPE(this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "TransactionComplete");

            HashMap<String, String> imap = new HashMap<>();
            imap.put("jsonData", jsonData);
            imap.put("authString", authString);

            boolean isInsert = true;
            ArrayList<HashMap<String, String>> alltranz = controller.getAllTransaction();
            if (alltranz != null && alltranz.size() > 0) {

                for (int i = 0; i < alltranz.size(); i++) {

                    if (jsonData.equalsIgnoreCase(alltranz.get(i).get("jsonData")) && authString.equalsIgnoreCase(alltranz.get(i).get("authString"))) {
                        isInsert = false;
                        break;
                    }
                }
            }


            if (isInsert && fillqty > 0) {
                controller.insertTransactions(imap);
            }


            //settransactionID to FSUNIT
            //==========================

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    new CommandsPOST().execute(URL_SET_TXNID, "{\"txtnid\":" + TransactionId + "}");

                    //new CommandsPOST().execute(URL_RELAY, jsonRelayOn);
                }
            }, 1500);

            //==========================
            Constants.AccVehicleNumber_FS1 = "";
            Constants.AccOdoMeter_FS1 = 0;
            Constants.AccDepartmentNumber_FS1 = "";
            Constants.AccPersonnelPIN_FS1 = "";
            Constants.AccOther_FS1 = "";
            //Constants.AccHours_FS1=0;


        } catch (Exception ex) {

            CommonUtils.LogMessage("APFS_PIPE", "AuthTestAsyncTask ", ex);
        }


        isTransactionComp = true;

        AppConstants.BUSY_STATUS = true;


        //btnStop.setVisibility(View.GONE);
        consoleString = "";
        //tvConsole.setText("");


        if (AppConstants.NeedToRename) {
            String userEmail = CommonUtils.getCustomerDetails_backgroundService_PIPE(this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = AppConstants.R_SITE_ID;
            rhose.HoseId = AppConstants.R_HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(this, AppConstants.NeedToRename, jsonData, authString);

        }


        startService(new Intent(this, BackgroundService.class));

        //linearFuelAnother.setVisibility(View.VISIBLE);

    }

    public void storeIsRenameFlag(Context context, boolean flag, String jsonData, String authString) {
        SharedPreferences pref;

        SharedPreferences.Editor editor;
        pref = context.getSharedPreferences("storeIsRenameFlag", 0);
        editor = pref.edit();


        // Storing
        editor.putBoolean("flag", flag);
        editor.putString("jsonData", jsonData);
        editor.putString("authString", authString);

        // commit changes
        editor.commit();


    }

    public class UpdateAsynTask extends AsyncTask<Void, Void, Void> {

        UpdateTransactionStatusClass authEntity = null;


        public String response = null;

        public UpdateAsynTask(UpdateTransactionStatusClass authEntity) {
            this.authEntity = authEntity;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(authEntity);
                String userEmail = CommonUtils.getCustomerDetails_backgroundService_PIPE(BackgroundService_AP_PIPE.this).PersonEmail;


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntity.IMEIUDID + ":" + userEmail + ":" + "UpgradeTransactionStatus");
                response = serverHandler.PostTextData(BackgroundService_AP_PIPE.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("", "UpgradeTransactionStatus ", ex);
            }
            return null;
        }

    }
}
