package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.UserInfoEntity;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.WifiEntityClass;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "MainActivity";
    Button setWifi;
    WifiManager wifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    List<WifiEntityClass> listOfProvider;
    ListAdapter adapter;
    GridView listViwProvider;
    private ProgressDialog pd;
    private ConnectionDetector cd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_wifi);

        //---------------------------------------------------------------------
        // Check Wifi enabled or not first.
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() == true) {

            scaning();
        } else {


            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Wifi disabled.");
            alertDialog.setMessage("Please check you wifi Enabled?");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    alertDialog.dismiss();

                    finish();
                }
            });
            alertDialog.show();

        }
        //--------------------------------------------------------------

        listOfProvider = new ArrayList<>();


        //--------------------------------------------------------------
        // Inflate GUI from xml
        InItGUI();
        //--------------------------------------------------------------


        listViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                cd = new ConnectionDetector(MainActivity.this);
                if (cd.isConnectingToInternet()) {


                    try {
                        if (cd.isConnectingToInternet()) {


                            String networkSSID = wifiList.get(position).SSID;
                            Toast.makeText(MainActivity.this, "" + networkSSID, Toast.LENGTH_SHORT).show();
                            networkSSID = networkSSID.replaceAll("^\"|\"$", "");
                            handleGetSitListTask(networkSSID);


                        } else if (discoonectToWifi()) {


                            String networkSSID = wifiList.get(position).SSID;
                            Toast.makeText(MainActivity.this, "" + networkSSID, Toast.LENGTH_SHORT).show();
                            networkSSID = networkSSID.replaceAll("^\"|\"$", "");
                            handleGetSitListTask(networkSSID);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        CommonUtils.LogMessage(TAG, "", e);

                    }
                } else {
                    CommonUtils.showNoInternetDialog(MainActivity.this);
                }


            }
        });
    }

    public boolean discoonectToWifi() {
        boolean isOkay = false;
        WifiManager wifiManager = (WifiManager)  getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() == true) {

            AppConstants.LAST_CONNECTED_SSID = wifiManager.getConnectionInfo().getSSID();

            wifiManager.disconnect();
            isOkay = true;
        }

        return isOkay;
    }

    private void InItGUI() {

        listViwProvider = (GridView) findViewById(R.id.list_view_wifi);
        setWifi = (Button) findViewById(R.id.btn_wifi);
    }

    private void scaning() {

        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();

    }


    private void handleGetSitListTask(String selectedSSID) {

        try {


            UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(MainActivity.this);
            //----------------------------------------------------------------------------------
            // get SSID List and its details network related
            GetSitListAsynTask getSitListAsynTask = new GetSitListAsynTask(userInfoEntity.PersonEmail, selectedSSID);
            getSitListAsynTask.execute();
            getSitListAsynTask.get();

            String siteResponse = getSitListAsynTask.response;

            if (siteResponse != null && !siteResponse.isEmpty()) {

                JSONObject jsonObjectSite = new JSONObject(siteResponse);
                String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                if (ResponseMessageSite.equalsIgnoreCase("success")) {


                    String dataSite = jsonObjectSite.getString(AppConstants.RES_DATA_SSID);

                    CommonUtils.SaveDataInPref(MainActivity.this, dataSite, Constants.PREF_COLUMN_SITE);

                    connectToSelectedWifi(dataSite, selectedSSID);


                } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                    String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    // set title
                    alertDialogBuilder.setIcon(R.drawable.ic_car);
                    alertDialogBuilder.setTitle("No Data Found");
                    alertDialogBuilder
                            .setMessage(ResponseTextSite)
                            .setCancelable(false)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
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

    private void connectToSelectedWifi(String dataSite, String selectedSSID) {

        try {

            ConnectWifiTask connectWifiTask = new ConnectWifiTask(dataSite, selectedSSID);
            connectWifiTask.execute();
            connectWifiTask.get();


            scaning();

            if (connectWifiTask.isConnected) {


                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Please wait..");
                pd.show();

                new android.os.Handler().postDelayed(new Runnable() {
                    public void run() {

                        pd.dismiss();

                        Toast.makeText(MainActivity.this, "DONE", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("MESSAGE", true);
                        setResult(Constants.CONNECTION_CODE, intent);
                        finish();

                    }
                }, 6000);


            } else {
                Toast.makeText(MainActivity.this, "Unable to connect to " + selectedSSID, Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {

            Log.e("", ex.getMessage());
        }
    }

    /*setting the functionality of ON/OFF button*/
    @Override
    public void onClick(View arg0) {
        /* if wifi is ON set it OFF and set button text "OFF" */
        if (wifiManager.isWifiEnabled() == true) {
            wifiManager.setWifiEnabled(false);
            setWifi.setText("OFF");
            listViwProvider.setVisibility(ListView.GONE);
        }
        /* if wifi is OFF set it ON
         * set button text "ON"
		 * and scan available wifi provider*/
        else if (wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
            setWifi.setText("ON");
            listViwProvider.setVisibility(ListView.VISIBLE);
            scaning();
        }
    }

    protected void onPause() {
        super.onPause();

        if (receiverWifi != null) {
            unregisterReceiver(receiverWifi);
        }
    }

    protected void onResume() {

        if (receiverWifi != null) {
            registerReceiver(receiverWifi, new IntentFilter(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        super.onResume();
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {
            wifiList = wifiManager.getScanResults();

			/* sorting of wifi provider based on level */
            Collections.sort(wifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult lhs, ScanResult rhs) {
                    return (lhs.level > rhs.level ? -1
                            : (lhs.level == rhs.level ? 0 : 1));
                }
            });
            listOfProvider.clear();
            String providerName, connectionStatus = "Disconnected";

            for (int i = 0; i < wifiList.size(); i++) {

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();
                String connetedSSID = String.valueOf(info.getSSID());

				/* to get SSID and BSSID of wifi provider*/
                providerName = (wifiList.get(i).SSID).toString();

                if (connetedSSID.contains(providerName)) {
                    connectionStatus = "Connected";
                } else {
                    connectionStatus = "Disconnected";
                }


                WifiEntityClass wifiEntityClass = new WifiEntityClass();
                wifiEntityClass.ssidName = providerName;
                wifiEntityClass.connectionStatus = connectionStatus;

                listOfProvider.add(wifiEntityClass);
            }
            /*setting list of all wifi provider in a List*/
            adapter = new ListAdapter(MainActivity.this, listOfProvider);
            listViwProvider.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    public class GetSitListAsynTask extends AsyncTask<Void, Void, Void> {

        String Email = null;
        String latLong = null;
        String response = null;

        public GetSitListAsynTask(String Email, String latLong) {
            this.Email = Email;
            this.latLong = latLong;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(MainActivity.this) + ":" + Email + ":" + "AndroidSSID");
                response = serverHandler.PostTextData(MainActivity.this, AppConstants.webURL, latLong, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }


    public class ConnectWifiTask extends AsyncTask<Void, Void, Void> {
        String selectedSSID = null;
        String dataSite = null;
        boolean isConnected = false;

        ConnectWifiTask(String dataSite, String selectedSSID) {
            this.selectedSSID = selectedSSID;
            this.dataSite = dataSite;
        }

        @Override
        protected Void doInBackground(Void... params) {

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String connetedSSID = String.valueOf(info.getSSID());

            String wifiPassword = "";


            try {
                if (dataSite != null) {
                    JSONArray jsonArray = new JSONArray(dataSite);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        wifiPassword = jsonObject.getString("Password");

                    }
                }
            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "", ex);
            }


            connetedSSID = connetedSSID.replaceAll("^\"|\"$", "");
            selectedSSID = selectedSSID.replaceAll("^\"|\"$", "");


            if (!selectedSSID.contains(connetedSSID)) {

                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                wifiConfiguration.SSID = String.format("\"%s\"", selectedSSID);
                //wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                wifiConfiguration.preSharedKey = String.format("\"%s\"", wifiPassword);

                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                int netId = wifiManager.addNetwork(wifiConfiguration);

                if (wifiManager.isWifiEnabled()) { //---wifi is turned on---
                    //---disconnect it first---
                    wifiManager.disconnect();
                } else { //---wifi is turned off---
                    //---turn on wifi---
                    wifiManager.setWifiEnabled(true);
                }
                wifiManager.enableNetwork(netId, true);
                isConnected = wifiManager.reconnect();
            } else {
                isConnected = true;
            }
            return null;
        }

    }
}
