package com.TrakEngineering.FluidSecureHubFOBapp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.RenameHose;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.TrazComp;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import static android.text.TextUtils.isEmpty;
import static com.TrakEngineering.FluidSecureHubFOBapp.WelcomeActivity.wifiApManager;
import static java.lang.String.format;


public class DisplayMeterActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //WifiManager wifiManager;
    private static final String TAG = "DisplayMeterActivity :";
    private String vehicleNumber, odometerTenths = "0", dNumber = "", pNumber = "", oText = "", hNumber = "";
    private TextView textDateTime, tvCounts, tvGallons;
    private TextView textOdometer, txtVehicleNumber, tvConsole;
    private Socket socket;
    private Button btnCancel, btnFuelAnotherYes, btnFuelAnotherNo;
    ProgressBar progressBar2;
    LinearLayout linearTimer;
    TextView tvCountDownTimer, tv_hoseConnected;
    LinearLayout linearFuelAnother;
    Integer Pulses = 0;

    private static final int SERVER_PORT = 2901;
    private static final String SERVER_IP = "192.168.4.1";


    Socket socketFS = new Socket();
    String consoleString = "", outputQuantity = "0";
    boolean stopTimer = true;
    DBController controller = new DBController(DisplayMeterActivity.this);
    String VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel;
    double minFuelLimit = 0, numPulseRatio = 0;
    long stopAutoFuelSeconds = 0;
    double fillqty = 0;
    ArrayList<HashMap<String, String>> quantityRecords = new ArrayList<>();
    SimpleDateFormat sdformat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    boolean isTransactionComp = false;

    String EMPTY_Val = "";

    public String HTTP_URL = "";
    public String URL_GET_TXNID = "";
    public String URL_SET_TXNID = "";
    //String HTTP_URL = "http://192.168.4.1:80/";

    String URL_GET_PULSAR = "";//HTTP_URL + "client?command=pulsar ";
    String URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";

    String URL_INFO = HTTP_URL + "client?command=info";
    String URL_STATUS = HTTP_URL + "client?command=status";
    String URL_RECORD = HTTP_URL + "client?command=record10";


    String URL_WIFI = HTTP_URL + "config?command=wifi";
    String URL_RELAY = HTTP_URL + "config?command=relay";

    String URL_GET_USER = HTTP_URL + "upgrade?command=getuser";
    String URL_RESET = HTTP_URL + "upgrade?command=reset";
    String URL_FILE_UPLOAD = HTTP_URL + "upgrade?command=start";

    String jsonRename;
    String jsonConnectWifi = "{\"Request\":  {\"Station\":{\"Connect_Station\":{\"ssid\":\"tenda\",\"password\":\"1234567890\",\"token\":\"1234567890123456789012345678901234567890\"}}}}";
    String jsonRelayOn = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":1}}";
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";

    String jsonPulsar = "{\"pulsar_request\":{\"counter_set\":1}}";
    String jsonPulsarOff = "{\"pulsar_request\":{\"counter_set\":0}}";


    public Network networkTransportWifi;

    boolean pulsarConnected = false;

    ConnectivityManager connection_manager;


    public static boolean BRisWiFiConnected;

    public static TextView tvStatus;
    public static Button btnStart;
    TimerTask mTimerTask;
    final Handler handler = new Handler();
    Timer t = new Timer();
    int attempt = 1;
    boolean Istimeout_Sec = true;
    boolean isTCancelled = false;
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    String TimeOutinMinute;
    GoogleApiClient mGoogleApiClient;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;


    double CurrentLat = 0, CurrentLng = 0;


    TextView tvWifiList;


    int timeThanks = 6;
    Timer tThanks;
    TimerTask taskThanks;


    // int timeFirst = 60;
    Timer tFirst;
    TimerTask taskFirst;

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (!CommonUtils.isHotspotEnabled(DisplayMeterActivity.this)) {

            btnStart.setText("Please wait..");
            btnStart.setEnabled(false);
            wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled
            AppConstants.colorToastBigFont(DisplayMeterActivity.this, "Connecting to hotspot, please wait", Color.RED);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    btnStart.setText("START");
                    btnStart.setEnabled(true);
                }
            }, 10000);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_meter);


        getSupportActionBar().setTitle(R.string.fs_name);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);

        InItGUI();
        tvWifiList = (TextView) findViewById(R.id.tvWifiList);

        SharedPreferences sharedPrefODO = DisplayMeterActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        IsOdoMeterRequire = sharedPrefODO.getString(AppConstants.IsOdoMeterRequire, "");
        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");
        TimeOutinMinute = sharedPrefODO.getString(AppConstants.TimeOut, "1");


        long screenTimeOut = Integer.parseInt(TimeOutinMinute) * 60000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Istimeout_Sec) {
                    Istimeout_Sec = false;
                    AppConstants.ClearEdittextFielsOnBack(DisplayMeterActivity.this);
                    Intent intent = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        }, screenTimeOut);

        getListOfConnectedDevice();

        /*
        LocationManager locationManager = (LocationManager) DisplayMeterActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        if (!statusOfGPS) {

            turnGPSOn();

        }*/


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();


        if (ActivityHandler.screenStack != null)
            ActivityHandler.screenStack.clear();

        vehicleNumber = Constants.AccVehicleNumber;
        odometerTenths = Constants.AccOdoMeter + "";
        dNumber = Constants.AccDepartmentNumber;
        pNumber = Constants.AccPersonnelPIN;
        oText = Constants.AccOther;
        hNumber = Constants.AccHours + "";


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


        //--------------------------------------------------------------------------
        // Display current date time
        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner(this, textDateTime);
        myThread = new Thread(myRunnableThread);
        myThread.start();
        //end current date time-----------------------------------------------------

        //textOdometer.setText(isEmpty(odometerTenths) ? "" : odometerTenths);
        // txtVehicleNumber.setText(isEmpty(vehicleNumber) ? "" : vehicleNumber);

        //--------------------------------------------------------

        String networkSSID;
        if (AppConstants.NeedToRename) {
            jsonRename = "{\"Request\":{\"SoftAP\":{\"Connect_SoftAP\":{\"authmode\":\"OPEN\",\"channel\":6,\"ssid\":\"" + AppConstants.REPLACEBLE_WIFI_NAME + "\",\"password\":\"\"}}}}";
        }
        networkSSID = AppConstants.LAST_CONNECTED_SSID;


        System.out.println("NeedToRename--" + AppConstants.NeedToRename);

        String networkPass = AppConstants.WIFI_PASSWORD;

        BRisWiFiConnected = false;


        //--------------------------------------------------

            SharedPreferences sharedPref = this.getSharedPreferences(Constants.PREF_VehiFuel, Context.MODE_PRIVATE);
            VehicleId = sharedPref.getString("VehicleId", "");
            PhoneNumber = sharedPref.getString("PhoneNumber", "");
            PersonId = sharedPref.getString("PersonId", "");
            PulseRatio = sharedPref.getString("PulseRatio", "1");
            MinLimit = sharedPref.getString("MinLimit", "0");
            FuelTypeId = sharedPref.getString("FuelTypeId", "");
            ServerDate = sharedPref.getString("ServerDate", "");
            IntervalToStopFuel = sharedPref.getString("IntervalToStopFuel", "0");

        minFuelLimit = Double.parseDouble(MinLimit);

        numPulseRatio = Double.parseDouble(PulseRatio);

        stopAutoFuelSeconds = Long.parseLong(IntervalToStopFuel);


        System.out.println("iiiiii" + IntervalToStopFuel);
        System.out.println("minFuelLimit" + minFuelLimit);
        System.out.println("getDeviceName" + minFuelLimit);

        String mobDevName = AppConstants.getDeviceName().toLowerCase();
        System.out.println("oooooooooo" + mobDevName);

        //Connect to bluetoothPrinter
        new SetBTConnectionPrinter().execute();


    }

    public void getListOfConnectedDevice() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                BufferedReader br = null;
                boolean isFirstLine = true;

                try {
                    br = new BufferedReader(new FileReader("/proc/net/arp"));
                    String line;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        String[] splitted = line.split(" +");

                        if (splitted != null && splitted.length >= 4) {

                            String ipAddress = splitted[0];
                            String macAddress = splitted[3];
                            System.out.println("IPAddress" + ipAddress);
                            boolean isReachable = InetAddress.getByName(
                                    splitted[0]).isReachable(500);  // this is network call so we cant do that on UI thread, so i take background thread.
                            if (isReachable) {
                                Log.d("Device Information", ipAddress + " : "
                                        + macAddress);
                            }


                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void turnGPSOn() {


        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationRequest mLocationRequest1 = new LocationRequest();
        mLocationRequest1.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .addLocationRequest(mLocationRequest1);


        LocationSettingsRequest mLocationSettingsRequest = builder.build();


        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("Splash", "All location settings are satisfied.");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("Splash", "Location settings are not satisfied. Show the user a dialog to" +
                                "upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(DisplayMeterActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("Splash", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("Splash", "Location settings are inadequate, and cannot be fixed here. Dialog " +
                                "not created.");
                        break;
                }
            }
        });


        //Intent in = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        //startActivity(in);

    }


    @Override
    public void onConnected(Bundle bundle) {
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            CurrentLat = mLastLocation.getLatitude();
            CurrentLng = mLastLocation.getLongitude();

            System.out.println("CCCrrr" + CurrentLat);
            System.out.println("CCCrrr" + CurrentLng);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void doTimerTask() {

        mTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {


                        System.out.println(Calendar.getInstance().getTime());


                        if (BRisWiFiConnected && AppConstants.getConnectedWifiName(DisplayMeterActivity.this).equalsIgnoreCase("\"" + AppConstants.LAST_CONNECTED_SSID + "\"")) {

                            tvStatus.setText("");
                            stopTask();


                        } else {
                            if (attempt >= 3) {
                                tvStatus.setText("");
                                stopTask();

                                if (!isTCancelled)
                                    AlertSettings(DisplayMeterActivity.this, "Unable to connect " + AppConstants.LAST_CONNECTED_SSID + "!\n\nPlease connect to " + AppConstants.LAST_CONNECTED_SSID + " manually using the 'WIFI settings' screen.\nThen hit back and click on the 'START' button to continue.");
                            } else {

                                /*
                                if (linearFuelAnother.getVisibility() != View.VISIBLE) {

                                    AppConstants.dontConnectWiFi(DisplayMeterActivity.this);

                                    connectToWiFiNew();

                                    attempt++;

                                    tvStatus.setText("Please wait...\nConnecting to '" + AppConstants.LAST_CONNECTED_SSID + "'" + "\nAttempt " + attempt + "/3");
                                } else {
                                    tvStatus.setText("");
                                    stopTask();
                                }
                                */
                            }

                        }


                    }
                });
            }
        };


        t.schedule(mTimerTask, 0, 15000);  //10seconds

    }

    public void stopTask() {

        attempt = 100;

        if (mTimerTask != null) {


            Log.d("TIMER", "timer canceled");
            mTimerTask.cancel();
        }

    }


    private void InItGUI() {
        try {

            //---TextView-------------
            textDateTime = (TextView) findViewById(R.id.textDateTime);
            tv_hoseConnected = (TextView) findViewById(R.id.tv_hoseConnected);
            textOdometer = (TextView) findViewById(R.id.textOdometer);
            txtVehicleNumber = (TextView) findViewById(R.id.txtVehicleNumber);
            tvCounts = (TextView) findViewById(R.id.tvCounts);
            tvGallons = (TextView) findViewById(R.id.tvGallons);
            tvConsole = (TextView) findViewById(R.id.tvConsole);
            tvCountDownTimer = (TextView) findViewById(R.id.tvCountDownTimer);

            linearTimer = (LinearLayout) findViewById(R.id.linearTimer);


            //-----------Buttons----
            btnStart = (Button) findViewById(R.id.btnStart);
            btnCancel = (Button) findViewById(R.id.btnCancel);
            linearFuelAnother = (LinearLayout) findViewById(R.id.linearFuelAnother);
            //btnFuelHistory = (Button) findViewById(R.id.btnFuelHistory);
            btnFuelAnotherYes = (Button) findViewById(R.id.btnFuelAnotherYes);
            btnFuelAnotherNo = (Button) findViewById(R.id.btnFuelAnotherNo);


            btnStart.setOnClickListener(this);
            btnCancel.setOnClickListener(this);
            // btnFuelHistory.setOnClickListener(this);
            btnFuelAnotherYes.setOnClickListener(this);
            btnFuelAnotherNo.setOnClickListener(this);


        } catch (Exception ex) {

            CommonUtils.LogMessage(TAG, " InItGUI ", ex);
        }
    }

    @Override
    public void onBackPressed() {

        Istimeout_Sec = false;
        Intent i = new Intent(this, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

    }


    @TargetApi(21)
    public void setGlobalMobileDatConnection() {

        NetworkRequest.Builder requestbuilder = new NetworkRequest.Builder();
        requestbuilder
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

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

    public void startWelcomeActityDiscWifi() {


        WelcomeActivity.SelectedItemPos = -1;

        Intent i = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }


    @SuppressLint({"ShowToast", "ResourceAsColor"})
    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.btnStart:

                btnStart.setClickable(false);
                btnStart.setBackgroundColor(Color.parseColor("#f5f1f0"));
                btnStart.setTextColor(R.color.black);
                btnCancel.setClickable(false);
                Istimeout_Sec = false;
                String macaddress = AppConstants.SELECTED_MACADDRESS;

                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (macaddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        String IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL = "http://" + IpAddress + ":80/";

                    }

                }
                URL_GET_TXNID = HTTP_URL + "client?command=lasttxtnid";
                URL_SET_TXNID = HTTP_URL + "config?command=txtnid";
                URL_GET_PULSAR = HTTP_URL + "client?command=pulsar ";
                URL_INFO = HTTP_URL + "client?command=info";

                String PulserTimingAd = HTTP_URL + "config?command=pulsar";
                URL_SET_PULSAR = HTTP_URL + "config?command=pulsar";

                //Check if Hose connected to hotspot or not
                try {
                    String FSStatus = new CommandsGET().execute(URL_INFO).get();
                    System.out.print("psssss" + FSStatus);
                    if (FSStatus.startsWith("{") && FSStatus.contains("Version")) {

                        GetLastTransaction();

                        String Result_PulserTimingAdjust = new CommandsPOST().execute(PulserTimingAd, "{\"pulsar_status\":{\"sampling_time_ms\":" + AppConstants.PulserTimingAdjust + "}}").get();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                //We use pumppff time insted pulseroff time
                                long pulsar_off_time = (stopAutoFuelSeconds * 1000) + 3000;
                                new CommandsPOST().execute(URL_SET_PULSAR, "{\"pulsar_status\":{\"pulsar_off_time\":" + pulsar_off_time + "}}");

                            }
                        }, 1000);


                        if (AppConstants.FS_selected.equalsIgnoreCase("0")) {


                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    // startService(new Intent(DisplayMeterActivity.this, BackgroundService_AP_PIPE.class));
                                    Intent serviceIntent = new Intent(DisplayMeterActivity.this, BackgroundService_AP_PIPE.class);
                                    serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                                    startService(serviceIntent);

                                    Intent i = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);

                                }
                            }, 1500);


                        } else if (AppConstants.FS_selected.equalsIgnoreCase("1")) {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //startService(new Intent(DisplayMeterActivity.this, BackgroundService_AP.class));
                                    Intent serviceIntent = new Intent(DisplayMeterActivity.this, BackgroundService_AP.class);
                                    serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                                    startService(serviceIntent);

                                    Intent i = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);

                                }
                            }, 1500);
                        } else if (AppConstants.FS_selected.equalsIgnoreCase("2")) {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //startService(new Intent(DisplayMeterActivity.this, BackgroundService_AP.class));
                                    Intent serviceIntent = new Intent(DisplayMeterActivity.this, BackgroundService_FS_UNIT_3.class);
                                    serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                                    startService(serviceIntent);

                                    Intent i = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);

                                }
                            }, 1500);
                        } else if (AppConstants.FS_selected.equalsIgnoreCase("3")) {

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //startService(new Intent(DisplayMeterActivity.this, BackgroundService_AP.class));
                                    Intent serviceIntent = new Intent(DisplayMeterActivity.this, BackgroundService_FS_UNIT_4.class);//change background service to fsunite3
                                    serviceIntent.putExtra("HTTP_URL", HTTP_URL);
                                    startService(serviceIntent);

                                    Intent i = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);

                                }
                            }, 1500);
                        }


                    } else {

                        AppConstants.colorToastBigFont(DisplayMeterActivity.this, " Link is unavailable", Color.RED);
                        AppConstants.ClearEdittextFielsOnBack(DisplayMeterActivity.this); //Clear EditText on move to welcome activity.
                        Intent intent = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);


                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                break;

            case R.id.btnCancel:

                Istimeout_Sec = false;
                finish();
                break;

            case R.id.btnFuelAnotherYes:


                startWelcomeActityDiscWifi();


                break;

            case R.id.btnFuelAnotherNo:


                /*
                if (AppConstants.IS_WIFI_ON) {
                    wifiManagerMM = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (!wifiManagerMM.isWifiEnabled()) {
                        wifiManagerMM.setWifiEnabled(true);
                        wifiManagerMM.disconnect();
                    }
                }*/


                finish();

                break;

        }
    }

    public void GetLastTransaction() {
        try {
            String LastTXNid = new CommandsGET().execute(URL_GET_TXNID).get();

            String respp = new CommandsGET().execute(URL_GET_PULSAR).get();

            if (LastTXNid.equals("-1")) {
                System.out.println(LastTXNid);
            } else {

                if (respp.contains("pulsar_status")) {
                    JSONObject jsonObject = new JSONObject(respp);
                    JSONObject joPulsarStat = jsonObject.getJSONObject("pulsar_status");
                    String counts = joPulsarStat.getString("counts");
                    String pulsar_status = joPulsarStat.getString("pulsar_status");
                    String pulsar_secure_status = joPulsarStat.getString("pulsar_secure_status");

                    Pulses = Integer.parseInt(counts);
                    double lastCnt = Double.parseDouble(counts);
                    double Lastqty = lastCnt / numPulseRatio; //convert to gallons
                    Lastqty = AppConstants.roundNumber(Lastqty, 2);

                    //-----------------------------------------------
                    try {

                        TrazComp authEntityClass = new TrazComp();
                        authEntityClass.TransactionId = LastTXNid;
                        authEntityClass.FuelQuantity = Lastqty;
                        authEntityClass.Pulses = Pulses;
                        authEntityClass.AppInfo = " Version:" + CommonUtils.getVersionCode(DisplayMeterActivity.this) + " " + AppConstants.getDeviceName() + " Android " + android.os.Build.VERSION.RELEASE + " ";
                        authEntityClass.TransactionFrom = "A";

                        Gson gson = new Gson();
                        String jsonData = gson.toJson(authEntityClass);

                        System.out.println("TrazComp......" + jsonData);
                        AppConstants.WriteinFile("DisplayMeterActivity~~~~~~~~~" + "LAST TRANS jsonData " + jsonData);

                        String userEmail = CommonUtils.getCustomerDetails(DisplayMeterActivity.this).PersonEmail;

                        String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(DisplayMeterActivity.this) + ":" + userEmail + ":" + "TransactionComplete");

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


                        if (isInsert && Lastqty > 0) {
                            controller.insertTransactions(imap);

                            AppConstants.WriteinFile("DisplayMeterActivity~~~~~~~~~" + "LAST TRANS SAVED in sqlite");
                        }


                    } catch (Exception ex) {

                        AppConstants.WriteinFile("DisplayMeterActivity~~~~~~~~~" + "LAST TRANS Exception " + ex.getMessage());
                    }


                }
            }

        } catch (Exception e) {
            AppConstants.WriteinFile("DisplayMeterActivity~~~~~~~~~" + "LastTXNid Ex:" + e.getMessage() + " ");
        }


    }


    public void stopButtonFunctionality() {


        quantityRecords.clear();

        btnStart.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        //btnFuelHistory.setVisibility(View.VISIBLE);
        consoleString = "";
        tvConsole.setText("");

        //it stops pulsar logic------
        stopTimer = false;


        new CommandsPOST().execute(URL_RELAY, jsonRelayOff);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    String cntA = "0", cntB = "0", cntC = "0";

                    for (int i = 0; i < 3; i++) {

                        String result = new GETFINALPulsar().execute(URL_GET_PULSAR).get();


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


        new CommandsPOST().execute(URL_SET_PULSAR, jsonPulsarOff);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (AppConstants.NeedToRename) {

                    consoleString += "RENAME:\n" + jsonRename;

                    new CommandsPOST().execute(URL_WIFI, jsonRename);

                }
            }

        }, 2500);

        long secondsTime = 3000;

        if (AppConstants.NeedToRename) {
            secondsTime = 5000;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //AppConstants.disconnectWiFi(DisplayMeterActivity.this);

                TransactionCompleteFunction();

            }

        }, secondsTime);
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

                        new GETPulsarQuantity().execute(URL_GET_PULSAR);

                    }

                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        }, 0, 2000);


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
                            AppConstants.colorToastBigFont(DisplayMeterActivity.this, "Auto Stop!\n\nQuantity is same for last " + stopAutoFuelSeconds + " seconds.", Color.BLUE);
                            stopButtonFunctionality();
                            stopTimer = false;

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


    public void AlertSettings(final Context ctx, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                }

        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void TransactionCompleteFunction() {


        try {


            TrazComp authEntityClass = new TrazComp();
            authEntityClass.PersonId = PersonId;
            authEntityClass.SiteId = AcceptVehicleActivity.SITE_ID;
            authEntityClass.VehicleId = VehicleId;
            authEntityClass.CurrentOdometer = odometerTenths;
            authEntityClass.FuelQuantity = fillqty;
            authEntityClass.FuelTypeId = FuelTypeId;
            authEntityClass.PhoneNumber = PhoneNumber;
            authEntityClass.WifiSSId = AppConstants.LAST_CONNECTED_SSID;
            authEntityClass.TransactionDate = ServerDate;
            authEntityClass.TransactionFrom = "A";
            authEntityClass.CurrentLat = "" + CurrentLat;
            authEntityClass.CurrentLng = "" + CurrentLng;
            authEntityClass.VehicleNumber = vehicleNumber;

            authEntityClass.DepartmentNumber = dNumber;
            authEntityClass.PersonnelPIN = pNumber;
            authEntityClass.Other = oText;
            authEntityClass.Hours = hNumber;

            Gson gson = new Gson();
            String jsonData = gson.toJson(authEntityClass);

            System.out.println("TrazComp......" + jsonData);

            String userEmail = CommonUtils.getCustomerDetails(DisplayMeterActivity.this).Email;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(DisplayMeterActivity.this) + ":" + userEmail + ":" + "TransactionComplete");

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

           /* Constants.AccVehicleNumber = "";
            Constants.AccOdoMeter = 0;
            Constants.AccDepartmentNumber = "";
            Constants.AccPersonnelPIN = "";
            Constants.AccOther = "";*/


        } catch (Exception ex) {

            CommonUtils.LogMessage(TAG, "AuthTestAsyncTask ", ex);
        }


        isTransactionComp = true;

        AppConstants.BUSY_STATUS = true;


        btnCancel.setVisibility(View.GONE);
        consoleString = "";
        tvConsole.setText("");


        if (AppConstants.NeedToRename) {
            String userEmail = CommonUtils.getCustomerDetails(DisplayMeterActivity.this).Email;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(DisplayMeterActivity.this) + ":" + userEmail + ":" + "SetHoseNameReplacedFlag");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = AppConstants.R_SITE_ID;
            rhose.HoseId = AppConstants.R_HOSE_ID;
            rhose.IsHoseNameReplaced = "Y";

            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);

            storeIsRenameFlag(DisplayMeterActivity.this, AppConstants.NeedToRename, jsonData, authString);

        }


        //startService(new Intent(DisplayMeterActivity.this, BackgroundService.class));

        //linearFuelAnother.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar2.setVisibility(View.GONE);
                startTimer();
                alertThankYou(tvGallons.getText().toString() + "\n" + tvCounts.getText().toString() + "\n\nThank You!!!");
            }
        }, 1500);


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

            if (uniqueSet.size() == 1) {
                return true;
            }
        }

        return false;
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

                consoleString += "OUTPUT- " + result + "\n";

                tvConsole.setText(consoleString);

                System.out.println(result);

                if (stopTimer)
                    pulsarQtyLogic(result);


            } catch (Exception e) {

                System.out.println(e);
            }

        }
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

                tvConsole.setText(consoleString);

                System.out.println(result);


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
                        AppConstants.colorToastBigFont(DisplayMeterActivity.this, "Auto Stop!\n\nPulsar disconnected", Color.BLUE);
                        stopButtonFunctionality();
                    }
                }


                convertCountToQuantity(counts);


                if (!pulsar_secure_status.trim().isEmpty()) {
                    secure_status = Integer.parseInt(pulsar_secure_status);

                    if (secure_status == 0) {
                        linearTimer.setVisibility(View.GONE);
                        tvCountDownTimer.setText("-");

                    } else if (secure_status == 1) {
                        linearTimer.setVisibility(View.VISIBLE);
                        tvCountDownTimer.setText("5");

                    } else if (secure_status == 2) {
                        linearTimer.setVisibility(View.VISIBLE);
                        tvCountDownTimer.setText("4");

                    } else if (secure_status == 3) {
                        linearTimer.setVisibility(View.VISIBLE);
                        tvCountDownTimer.setText("3");

                    } else if (secure_status == 4) {
                        linearTimer.setVisibility(View.VISIBLE);
                        tvCountDownTimer.setText("2");

                    } else if (secure_status >= 5) {
                        linearTimer.setVisibility(View.GONE);
                        tvCountDownTimer.setText("1");

                        AppConstants.colorToastBigFont(DisplayMeterActivity.this, "Auto Stop!\n\nCount down timer completed.", Color.BLUE);
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

                            AppConstants.colorToastBigFont(DisplayMeterActivity.this, "Auto Stop!\n\nYou reached MAX fuel limit.", Color.BLUE);
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

    public void convertCountToQuantity(String counts) {
        outputQuantity = counts;

        fillqty = Double.parseDouble(outputQuantity);
        fillqty = fillqty / numPulseRatio;//convert to gallons

        fillqty = AppConstants.roundNumber(fillqty, 2);


        tvCounts.setText("Pulse: " + outputQuantity);
        tvGallons.setText("Quantity: " + (fillqty));

    }


    public class CommandsGET extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(DisplayMeterActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

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

            pd.dismiss();
            try {

                consoleString += "OUTPUT- " + result + "\n";

                tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    public class CommandsPOST extends AsyncTask<String, Void, String> {

        public String resp = "";

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(DisplayMeterActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL);
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

            pd.dismiss();
            try {

                consoleString += "OUTPUT- " + result + "\n";

                tvConsole.setText(consoleString);

                System.out.println(result);

            } catch (Exception e) {

                System.out.println(e);
            }

        }
    }

    /*public void startTimerForQuantityCheck(long millisInFuture) {

        //30000 -- 30 seconds
        long countDownInterval = 1000; //1 second

        CountDownTimer timer = new CountDownTimer(millisInFuture, countDownInterval) {
            public void onTick(long millisUntilFinished) {
                //do something in every tick
            }

            public void onFinish() {
                System.out.println("CountDownTimer...onFinish");


                quantityRecords.clear();
            }
        }.start();
    }
*/


    public void connectToWiFiOld() {

        tvWifiList.setText("");

        Log.i(TAG, "* connectToAP");
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        List<ScanResult> scanResultList = wifiManager.getScanResults();

        /*
        String wifiavailList = "";
        for (ScanResult result : scanResultList) {
            wifiavailList += result.SSID + "\n";
        }

        tvWifiList.setText(wifiavailList);
        */

        String networkSSID = AppConstants.LAST_CONNECTED_SSID;
        String networkPass = AppConstants.WIFI_PASSWORD;

        Log.d(TAG, "# password " + networkPass);

        for (ScanResult result : scanResultList) {
            if (result.SSID.equals(networkSSID)) {

                String securityMode = getScanResultSecurity(result);
                Log.d(TAG, "# securityMode " + securityMode);

                if (securityMode.equalsIgnoreCase("OPEN")) {

                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int resW = wifiManager.addNetwork(wifiConfiguration);
                    Log.d(TAG, "# add Network returned " + resW);

                    if (resW == -1) {
                        resW = getExistingNetworkId(networkSSID);
                    }

                    if (resW != -1) {
                        wifiManager.enableNetwork(resW, true);
                    }

                    wifiManager.setWifiEnabled(true);

                    break;

                } else if (securityMode.equalsIgnoreCase("WEP")) {

                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                    wifiConfiguration.wepTxKeyIndex = 0;
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    Log.d(TAG, "### 1 ### add Network returned " + res);

                    boolean b = wifiManager.enableNetwork(res, true);
                    Log.d(TAG, "# enableNetwork returned " + b);

                    wifiManager.setWifiEnabled(true);
                } else {

                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
                    wifiConfiguration.hiddenSSID = true;
                    wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);


                    int res = wifiManager.addNetwork(wifiConfiguration);
                    Log.d(TAG, "### 2 ### add Network returned " + res);

                    // wifiManager.enableNetwork(res, true);

                    boolean changeHappen = wifiManager.saveConfiguration();

                    if (res != -1 && changeHappen) {
                        Log.d(TAG, "### Change happen");


                    } else {
                        Log.d(TAG, "*** Change NOT happen");
                    }

                    wifiManager.setWifiEnabled(true);
                }
            }

        }


    }

    public String getScanResultSecurity(ScanResult scanResult) {
        Log.i(TAG, "* getScanResultSecurity");

        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK", "EAP"};

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void connectToWiFiNew() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(true);

            Thread.sleep(2000);


            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = String.format("\"%s\"", AppConstants.LAST_CONNECTED_SSID);
            wifiConfig.preSharedKey = String.format("\"%s\"", AppConstants.WIFI_PASSWORD);


            //remember id
            int netId = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();
            Thread.sleep(2000);
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private int getExistingNetworkId(String SSID) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID.equals(SSID)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void alertThankYou(String msg) {
        final Dialog dialog = new Dialog(DisplayMeterActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_thankyou);
        dialog.setCancelable(false);


        TextView tvText = (TextView) dialog.findViewById(R.id.tvText);

        tvText.setText(msg);


        final Button btnDailogOk = (Button) dialog.findViewById(R.id.btnOk);

        btnDailogOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                stopThankYouTimer();

                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void stopThankYouTimer() {
        tThanks.cancel();
        tThanks.purge();

        WelcomeActivity.SelectedItemPos = -1;

        Intent i = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    public class AboveVessionHttp extends AsyncTask<Network, Void, String> {

        String cmdURL;
        String cmdJSON;

        public AboveVessionHttp(String cmdURL, String cmdJSON) {
            this.cmdURL = cmdURL;
            this.cmdJSON = cmdJSON;

        }

        protected String doInBackground(Network... ntwrk) {
            String resp = "";


            try {

                resp = sendCommandViaWiFi(ntwrk[0], cmdURL, cmdJSON);


            } catch (Exception e) {
                System.out.println(e);
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            System.out.println("MM-" + result);

            consoleString += "OUTPUT- " + result + "\n";

            tvConsole.setText(consoleString);


            if (result.contains("pulsar_status")) {

                if (stopTimer)
                    pulsarQtyLogic(result);
            }
        }
    }


    @TargetApi(21)
    private void setHttpTransportWifi(final String cmdURL, final String cmdJSON) {


        NetworkRequest.Builder requestbuilder = new NetworkRequest.Builder();
        requestbuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        cm.requestNetwork(requestbuilder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                System.out.println("wifi network found");

                new AboveVessionHttp(cmdURL, cmdJSON).execute(network);

            }
        });

    }


    @TargetApi(21)
    private String sendCommandViaWiFi(Network network, String URLName, String jsonN) {
        String ress = "";
        try {


            // client one, should go via wifi
            okhttp3.OkHttpClient.Builder builder1 = new okhttp3.OkHttpClient.Builder();
            builder1.socketFactory(network.getSocketFactory());

            okhttp3.OkHttpClient client1 = builder1.build();

            okhttp3.Request request1;

            if (jsonN.equalsIgnoreCase(EMPTY_Val)) {
                request1 = new okhttp3.Request.Builder()
                        .url(URLName)
                        .build();
            } else {

                okhttp3.MediaType JSON = okhttp3.MediaType.parse("application/json");


                okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, jsonN);


                request1 = new okhttp3.Request.Builder()
                        .url(URLName)
                        .post(body)
                        .build();
            }


            System.out.println("sending via wifi network");

            okhttp3.Response response = client1.newCall(request1).execute();

            ress = response.body().string();


        } catch (Exception e) {
            System.out.println(e);
        }
        return ress;
    }


    /////////////////////////////////////////////////////////

    /*public boolean connectToSSID(String SSID) {

        WifiConfiguration configuration = createOpenWifiConfiguration(SSID);
        int networkId = wifiManagerMM.addNetwork(configuration);
        Log.d("", "networkId assigned while adding network is " + networkId);
        return enableNetwork(SSID, networkId);
    }

    private WifiConfiguration createOpenWifiConfiguration(String SSID) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = formatSSID(SSID);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        assignHighestPriority(configuration);
        return configuration;
    }



    private boolean enableNetwork(String SSID, int networkId) {
        if (networkId == -1) {
            networkId = getExistingNetworkId(SSID);

            if (networkId == -1) {
                Log.e("ssss", "Couldn't add network with SSID: " + SSID);
                return false;
            }
        }
        return wifiManagerMM.enableNetwork(networkId, true);
    }


    //To tell OS to give preference to this network
    private void assignHighestPriority(WifiConfiguration config) {
        List<WifiConfiguration> configuredNetworks = wifiManagerMM.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (config.priority <= existingConfig.priority) {
                    config.priority = existingConfig.priority + 1;
                }
            }
        }
    }
    */

    private static String formatSSID(String wifiSSID) {
        return format("\"%s\"", wifiSSID);
    }

    private static String trimQuotes(String str) {
        if (!isEmpty(str)) {
            return str.replaceAll("^\"*", "").replaceAll("\"*$", "");
        }

        return str;
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


    private class WiFiConnectTask extends AsyncTask<String, Void, String> {
        // Do the long-running work in here
        protected String doInBackground(String... asd) {


            connectToWiFiOld();


            return "";
        }


        @Override
        protected void onPostExecute(String s) {


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {


                    /*
                    if (!BRisWiFiConnected || !AppConstants.getConnectedWifiName(DisplayMeterActivity.this).equalsIgnoreCase("\"" + AppConstants.LAST_CONNECTED_SSID + "\"")) {

                        if (linearFuelAnother.getVisibility() != View.VISIBLE) {

                            //attempt = 4;
                            //doTimerTask();
                        }
                    }
                    */

                    if (BRisWiFiConnected && AppConstants.getConnectedWifiName(DisplayMeterActivity.this).equalsIgnoreCase("\"" + AppConstants.LAST_CONNECTED_SSID + "\"")) {

                        tvStatus.setText("");

                    } else {

                        tvStatus.setText("");

                        if (!isTCancelled)
                            AlertSettings(DisplayMeterActivity.this, "Unable to connect " + AppConstants.LAST_CONNECTED_SSID + "!\n\nPlease connect to " + AppConstants.LAST_CONNECTED_SSID + " manually using the 'WIFI settings' screen.\nThen hit back and click on the 'START' button to continue.");

                    }


                }
            }, 6000);

        }
    }


    public void startTimer() {
        tThanks = new Timer();
        taskThanks = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (timeThanks > 0)
                            timeThanks -= 1;
                        else {
                            stopThankYouTimer();
                        }
                    }
                });
            }
        };
        tThanks.scheduleAtFixedRate(taskThanks, 0, 1000);
    }


    public void connectToWifiMarsh() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
            WifiConfiguration wc = new WifiConfiguration();

            wc.SSID = "\"" + AppConstants.LAST_CONNECTED_SSID + "\"";
            wc.preSharedKey = "\"\"";
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiManager.setWifiEnabled(true);
            int netId = wifiManager.addNetwork(wc);
            if (netId == -1) {
                netId = getExistingNetworkId(AppConstants.LAST_CONNECTED_SSID);
            }
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


  /*  public void startTimerFirst() {
        tFirst = new Timer();
        taskFirst = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (timeFirst > 0)
                            timeFirst -= 1;
                        else {
                            stopFirstTimer(false);
                        }
                    }
                });
            }
        };
        tFirst.scheduleAtFixedRate(taskFirst, 0, 1000);
    }*/

    public void stopFirstTimer(boolean flag) {
        if (flag) {
            tFirst.cancel();
            tFirst.purge();
        } else {
            tFirst.cancel();
            tFirst.purge();

            WelcomeActivity.SelectedItemPos = -1;
            AppConstants.BUSY_STATUS = true;

            Intent i = new Intent(DisplayMeterActivity.this, WelcomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    public class SetBTConnectionPrinter extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {


            try {
                BluetoothPrinter.findBT();
                BluetoothPrinter.openBT();

                System.out.println("printer. FindBT and OpenBT");
             } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }


}
