package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

/**
 * Created by Administrator on 2/3/2017.
 */


public class MobileDataReceiver extends BroadcastReceiver {
    private static boolean firstConnect = true;

    @Override
    public void onReceive(Context context, Intent intent) {

        DBController controller = new DBController(context);


        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            if (!noConnectivity) {

                Log.d("MobileDataReceiver", "connected");

                if (firstConnect) {

                    firstConnect = false;
                    Log.d("MobileDataReceiver", "call only one time...");


                    /*
                    ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

                    if (uData != null && uData.size() > 0) {
                        context.startService(new Intent(context, BackgroundService.class));
                        System.out.println("BackgroundService Start...");
                    } else {
                        context.stopService(new Intent(context, BackgroundService.class));
                        System.out.println("BackgroundService STOP...");
                    }

                    */
                }

            } else {
                Log.d("MobileDataReceiver", "disconnected");

                firstConnect = true;
            }
        }
    }

}