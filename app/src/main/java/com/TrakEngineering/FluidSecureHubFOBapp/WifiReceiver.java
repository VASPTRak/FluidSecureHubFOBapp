package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Administrator on 3/23/2017.
 */

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        //System.out.println("WiFi status " + info.getState().name());
        //System.out.println("WiFi Detailed status " + info.getDetailedState().name());

        if (info != null && info.isConnected()) {
            // Do your work.

            // e.g. To check the Network Name or other info:
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            if (ssid.equalsIgnoreCase("\"" + AppConstants.LAST_CONNECTED_SSID + "\"")) {

                DisplayMeterActivity.BRisWiFiConnected = true;
                DisplayMeterActivity.btnStart.setEnabled(true);
              //  DisplayMeterActivity.tvStatus.setText("");

                

            }
            System.out.println("WiFi connected to " + ssid);
            System.out.println("WiFi connected to " + wifiInfo.getNetworkId());
        }
    }
}