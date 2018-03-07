package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.RenameHose;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.UserInfoEntity;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/*public class SelectFSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_fs);
    }
}*/

public class SelectFSActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String TAG = " WelcomeActivity ";
    private float density;

    private TextView textDateTime;
    private ImageView imgFuelLogo;

    private TextView tvTitle;
    private Button btnGo;
    private ConnectionDetector cd;
    private double latitude = 0;
    private double longitude = 0;
    TextView tvSSIDName;
    LinearLayout linearHose;

    //ArrayList<String> ssidList = new ArrayList<>();
    ArrayList<HashMap<String, String>> serverSSIDList = new ArrayList<>();
    public static int SelectedItemPos;


    GoogleApiClient mGoogleApiClient;

    ConnectivityManager connection_manager;


    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    TextView tvLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_fs);
        tvSSIDName = (TextView) findViewById(R.id.tvSSIDName);
        tvLatLng = (TextView) findViewById(R.id.tvLatLng);

        tvLatLng.setVisibility(View.GONE);

        SelectedItemPos = -1;

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setIcon(R.drawable.fuel_secure_lock);

        density = getResources().getDisplayMetrics().density;

        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(SelectFSActivity.this));


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        //AppConstants.disconnectWiFi(SelectFSActivity.this);

        InItGUI();


        // set User Information
        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(SelectFSActivity.this);

        AppConstants.Title = "Name : " + userInfoEntity.PersonName + "\nMobile : " + userInfoEntity.PhoneNumber + "\nEmail : " + userInfoEntity.PersonEmail;

        tvTitle = (TextView) findViewById(R.id.textView);
        tvTitle.setText(AppConstants.Title);

        //------------------------------------------------------------------------------------------


        //-----------------------------------------------------------------
        // Display current date time u
        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner(this, textDateTime);
        myThread = new Thread(myRunnableThread);
        myThread.start();
        //end current date time----------------------------------------------

        //RefreshHoseList
        refreshWiFiList();


        if (ActivityHandler.screenStack != null)
            ActivityHandler.screenStack.clear();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (AppConstants.BUSY_STATUS)
                    new ChangeBusyStatus().execute();

                String mobDevName = AppConstants.getDeviceName().toLowerCase();
                System.out.println("oooooooooo" + mobDevName);
                if (mobDevName.contains("moto") && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    DBController controller = new DBController(SelectFSActivity.this);
                    ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

                    if (uData != null && uData.size() > 0) {
                        startService(new Intent(SelectFSActivity.this, BackgroundService.class));
                        System.out.println("BackgroundService Start...");
                    } else {
                        stopService(new Intent(SelectFSActivity.this, BackgroundService.class));
                        System.out.println("BackgroundService STOP...");
                    }
                }
            }
        }, 2000);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.first, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mClose:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            System.out.println("rrr" + String.valueOf(mLastLocation.getLatitude()));
            System.out.println("rrr" + String.valueOf(mLastLocation.getLongitude()));


            LocationManager locationManager = (LocationManager) SelectFSActivity.this.getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


            /*
            if (!statusOfGPS) {
                latitude = 0;
                longitude = 0;
            } else {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }
            */

           /*
            if (latitude == 0 && longitude == 0) {
                AppConstants.AlertDialogFinish(WelcomeActivity.this, "Unable to get current location.\nPlease try again later!");
            }
            */

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void InItGUI() {

        textDateTime = (TextView) findViewById(R.id.textDateTime);

        imgFuelLogo = (ImageView) findViewById(R.id.imgFuelLogo);
        linearHose = (LinearLayout) findViewById(R.id.linearHose);

        btnGo = (Button) findViewById(R.id.btnGo);
    }

    public void selectHoseAction(View v) {
        refreshWiFiList();
    }

    public void goButtonAction(View view) {
        try {


            boolean flagGo = false;

            LocationManager locationManager = (LocationManager) SelectFSActivity.this.getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                if (!statusOfGPS) {
                    turnGPSOn();
                } else {
                    flagGo = true;
                }

            } else {
                flagGo = true;

            }


            if (flagGo) {

                if (SelectedItemPos >= 0) {

                    if (serverSSIDList.size() > 0) {

                        String selectedSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                        String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
                        String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
                        String SiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                        String HoseId = serverSSIDList.get(SelectedItemPos).get("HoseId");

                        AppConstants.LAST_CONNECTED_SSID = selectedSSID;

                        if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {

                            AppConstants.NeedToRename = false;

                            AppConstants.REPLACEBLE_WIFI_NAME = "";
                            AppConstants.R_HOSE_ID = "";
                            AppConstants.R_SITE_ID = "";

                        } else {
                            AppConstants.NeedToRename = true;

                            AppConstants.REPLACEBLE_WIFI_NAME = ReplaceableHoseName;
                            AppConstants.R_HOSE_ID = HoseId;
                            AppConstants.R_SITE_ID = SiteId;

                        }

                        AppConstants.R_SITE_ID = SiteId;

                        AuthEntityClass authEntityClass = CommonUtils.getWiFiDetails(SelectFSActivity.this, selectedSSID);

                        if (authEntityClass != null) {


                            cd = new ConnectionDetector(SelectFSActivity.this);
                            if (cd.isConnectingToInternet()) {

                                handleGetAndroidSSID(selectedSSID);

                            } else {
                                CommonUtils.showNoInternetDialog(SelectFSActivity.this);
                            }

                        } else {
                            Toast.makeText(SelectFSActivity.this, "Please try later.", Toast.LENGTH_SHORT).show();
                        }

                        /*
                           // if (ssidList.contains(serverSSIDList.get(SelectedItemPos).get("item"))) {

                        } else {
                            AppConstants.AlertDialogBox(WelcomeActivity.this, "Fuel site not available at this location\nPlease try again.");

                            scanLocalWiFi();
                        }*/

                    } else {
                        AppConstants.AlertDialogBox(SelectFSActivity.this, "Unable to get Fluid Secure list from server");
                    }
                } else {
                    AppConstants.AlertDialogBox(SelectFSActivity.this, "Please select Hose");
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }




    private void handleGetAndroidSSID(String selectedSSID) {

        try {


            UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(SelectFSActivity.this);
            //----------------------------------------------------------------------------------

            selectedSSID += "#:#0#:#0";

            System.out.println("selectedSSID.." + selectedSSID);

            GetAndroidSSID getSitListAsynTask = new GetAndroidSSID(userInfoEntity.PersonEmail, selectedSSID);
            getSitListAsynTask.execute();
            getSitListAsynTask.get();

            String siteResponse = getSitListAsynTask.response;

            if (siteResponse != null && !siteResponse.isEmpty()) {

                JSONObject jsonObjectSite = new JSONObject(siteResponse);
                String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                if (ResponseMessageSite.equalsIgnoreCase("success")) {


                    String dataSite = jsonObjectSite.getString(AppConstants.RES_DATA_SSID);

                    CommonUtils.SaveDataInPref(SelectFSActivity.this, dataSite, Constants.PREF_COLUMN_SITE);

                    startWelcomeActivity();


                } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                    String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SelectFSActivity.this);
                    // set title

                    alertDialogBuilder.setTitle("Fuel Secure");
                    alertDialogBuilder
                            .setMessage(ResponseTextSite)
                            .setCancelable(false)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {

                                    dialog.cancel();
                                }
                            });
                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            }
            //-------------------------------------------------------------------------
        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "handleGetSitListTask", ex);
        }
    }

    private void startWelcomeActivity() {

        Intent intent = new Intent(SelectFSActivity.this, AcceptVehicleActivity.class);
        startActivity(intent);
    }

    public class GetAndroidSSID extends AsyncTask<Void, Void, Void> {

        String Email = null;
        String latLong = null;
        String response = null;

        public GetAndroidSSID(String Email, String latLong) {
            this.Email = Email;
            this.latLong = latLong;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(SelectFSActivity.this) + ":" + Email + ":" + "AndroidSSID");
                response = serverHandler.PostTextData(SelectFSActivity.this, AppConstants.webURL, latLong, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }

    public void onChangeWifiAction(View view) {
        try {

            refreshWiFiList();


        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "onChangeWifiAction :", ex);
        }
    }

    public void refreshWiFiList() {
        new GetSSIDUsingLocation().execute();
    }


    /*
    public void scanLocalWiFi() {

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                List<ScanResult> mScanResults = wifiManager.getScanResults();

                ssidList.clear();
                for (ScanResult results : mScanResults) {
                    System.out.println("result...." + results.SSID);
                    ssidList.add(results.SSID);
                }


                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                }
            }
        }, 3000);

    }
    */

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
                            status.startResolutionForResult(SelectFSActivity.this, REQUEST_CHECK_SETTINGS);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.CONNECTION_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                String messageData = data.getStringExtra("MESSAGE");

                if (messageData.equalsIgnoreCase("true")) {
                    Intent intent = new Intent(SelectFSActivity.this, AcceptVehicleActivity.class);
                    startActivity(intent);
                }
            }
        }

        /////////////////////////////////////////////

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Splash", "User agreed to make required location settings changes.");

                        AppConstants.colorToast(getApplicationContext(), "Please wait...", Color.BLACK);


                        goButtonAction(null);

                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("Splash", "User chose not to make required location settings changes.");

                        AppConstants.colorToastBigFont(getApplicationContext(), "Please On GPS to connect WiFi", Color.BLUE);

                        break;
                }
                break;
        }
    }


    public class GetSSIDUsingLocation extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(SelectFSActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(true);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(SelectFSActivity.this);

                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String parm1 = AppConstants.getIMEI(SelectFSActivity.this) + ":" + userInfoEntity.PersonEmail + ":" + "Other";
                String parm2 = "Authenticate:I:" + latitude + "," + longitude;


                System.out.println("parm1----" + parm1);
                System.out.println("parm2----" + parm2);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                //resp = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, parm2, authString);
                //----------------------------------------------------------------------------------
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                client.setWriteTimeout(10, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(ServerHandler.TEXT, parm2);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();


                //------------------------------

            } catch (Exception e) {
                System.out.println("Ex" + e.getMessage());
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            tvLatLng.setText("Current Location :" + latitude + "," + longitude);

            System.out.println("GetSSIDUsingLocation...." + result);

            try {

                serverSSIDList.clear();

                String errMsg = "";

                if (result != null && !result.isEmpty()) {

                    JSONObject jsonObjectSite = new JSONObject(result);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

                        JSONArray Requests = jsonObjectSite.getJSONArray(AppConstants.RES_DATA_SSID);

                        if (Requests.length() > 0) {

                            for (int i = 0; i < Requests.length(); i++) {
                                JSONObject c = Requests.getJSONObject(i);


                                String SiteId = c.getString("SiteId");
                                String SiteNumber = c.getString("SiteNumber");
                                String SiteName = c.getString("SiteName");
                                String SiteAddress = c.getString("SiteAddress");
                                String Latitude = c.getString("Latitude");
                                String Longitude = c.getString("Longitude");
                                String HoseId = c.getString("HoseId");
                                String HoseNumber = c.getString("HoseNumber");
                                String WifiSSId = c.getString("WifiSSId");
                                String UserName = c.getString("UserName");
                                String Password = c.getString("Password");
                                String ResponceMessage = c.getString("ResponceMessage");
                                String ResponceText = c.getString("ResponceText");
                                String ReplaceableHoseName = c.getString("ReplaceableHoseName");
                                String IsHoseNameReplaced = c.getString("IsHoseNameReplaced");
                                String MacAddress = c.getString("MacAddress");
                                String IsBusy = c.getString("IsBusy");

                                HashMap<String, String> map = new HashMap<>();
                                map.put("SiteId", SiteId);
                                map.put("HoseId", HoseId);
                                map.put("WifiSSId", WifiSSId);
                                map.put("ReplaceableHoseName", ReplaceableHoseName);
                                map.put("IsHoseNameReplaced", IsHoseNameReplaced);
                                map.put("item", WifiSSId);
                                map.put("MacAddress", MacAddress);
                                map.put("IsBusy", IsBusy);

                                if (ResponceMessage.equalsIgnoreCase("success")) {
                                    if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                        serverSSIDList.add(map);
                                    }
                                } else {
                                    errMsg = ResponceText;
                                }
                            }


                        }

                        alertSelectHoseList(tvLatLng.getText().toString() + "\n" + errMsg);

                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                        String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                        AppConstants.AlertDialogBox(SelectFSActivity.this, ResponseTextSite);


                    }
                } else {
                    AppConstants.AlertDialogFinish(SelectFSActivity.this, "Unable to connect server. Please try again later!");
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " GetSSIDUsingLocation :" + result, e);
            }

        }
    }


    public boolean isNotNULL(String value) {

        boolean flag = true;
        if (value == null) {
            flag = false;
        } else if (value.trim().isEmpty()) {
            flag = false;
        } else if (value != null && value.trim().equalsIgnoreCase("null")) {
            flag = false;
        }

        return flag;
    }

    public void alertSelectHoseList(String errMsg) {


        TextView tvNoFuelSites = (TextView) findViewById(R.id.tvNoFuelSites);
        ListView lvHoseNames = (ListView) findViewById(R.id.lvHoseNames);


        if (!errMsg.trim().isEmpty())
            tvNoFuelSites.setText(errMsg);

        if (serverSSIDList != null && serverSSIDList.size() > 0) {

            lvHoseNames.setVisibility(View.VISIBLE);
            tvNoFuelSites.setVisibility(View.GONE);

        } else {
            lvHoseNames.setVisibility(View.GONE);
            tvNoFuelSites.setVisibility(View.VISIBLE);
        }

        SimpleAdapter adapter = new SimpleAdapter(SelectFSActivity.this, serverSSIDList, R.layout.item_hose, new String[]{"item"}, new int[]{R.id.tvSingleItem});
        lvHoseNames.setAdapter(adapter);


        lvHoseNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelectedItemPos = position;

                String selSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                String IsBusy = serverSSIDList.get(SelectedItemPos).get("IsBusy");

                if (IsBusy.equalsIgnoreCase("Y")) {
                    tvSSIDName.setText("Hose in use.\nPlease try again later");
                    btnGo.setVisibility(View.GONE);
                } else {

                    tvSSIDName.setText(selSSID);
                    btnGo.setVisibility(View.VISIBLE);

                    if (SelectedItemPos >= 0) {

                        if (serverSSIDList.size() > 0) {

                            String selectedSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                            String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
                            String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
                            String SiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                            String HoseId = serverSSIDList.get(SelectedItemPos).get("HoseId");

                            AppConstants.LAST_CONNECTED_SSID = selectedSSID;

                            if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {

                                AppConstants.NeedToRename = false;

                                AppConstants.REPLACEBLE_WIFI_NAME = "";
                                AppConstants.R_HOSE_ID = "";
                                AppConstants.R_SITE_ID = "";

                            } else {
                                AppConstants.NeedToRename = true;

                                AppConstants.REPLACEBLE_WIFI_NAME = ReplaceableHoseName;
                                AppConstants.R_HOSE_ID = HoseId;
                                AppConstants.R_SITE_ID = SiteId;

                            }

                            AppConstants.R_SITE_ID = SiteId;

                            AuthEntityClass authEntityClass = CommonUtils.getWiFiDetails(SelectFSActivity.this, selectedSSID);

                            if (authEntityClass != null) {


                                cd = new ConnectionDetector(SelectFSActivity.this);
                                if (cd.isConnectingToInternet()) {

                                    handleGetAndroidSSID(selectedSSID);

                                } else {
                                    CommonUtils.showNoInternetDialog(SelectFSActivity.this);
                                }

                            } else {
                                Toast.makeText(SelectFSActivity.this, "Please try later.", Toast.LENGTH_SHORT).show();
                            }

                        /*
                           // if (ssidList.contains(serverSSIDList.get(SelectedItemPos).get("item"))) {

                        } else {
                            AppConstants.AlertDialogBox(WelcomeActivity.this, "Fuel site not available at this location\nPlease try again.");

                            scanLocalWiFi();
                        }*/

                        } else {
                            AppConstants.AlertDialogBox(SelectFSActivity.this, "Unable to get Fluid Secure list from server");
                        }
                    } else {
                        AppConstants.AlertDialogBox(SelectFSActivity.this, "Please select Hose");
                    }
                }

            }

        });

    }

    public class ChangeBusyStatus extends AsyncTask<String, Void, String> {


        //ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            // pd = new ProgressDialog(DisplayMeterActivity.this);
            // pd.setMessage("Please wait...");
            // pd.setCancelable(false);
        }

        protected String doInBackground(String... param) {
            String resp = "";

            String userEmail = CommonUtils.getCustomerDetails(SelectFSActivity.this).Email;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(SelectFSActivity.this) + ":" + userEmail + ":" + "ChangeBusyStatus");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = AppConstants.R_SITE_ID;


            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);


            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
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

                // pd.dismiss();

                System.out.println("eeee" + result);


            } catch (Exception e) {
                System.out.println("eeee" + e);
            }
        }


    }


}
