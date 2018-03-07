package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import static com.TrakEngineering.FluidSecureHubFOBapp.WelcomeActivity.wifiApManager;

/**
 * Created by User on 11/8/2017.
 */

public class BackgroundServiceHotspotCheck extends BackgroundService{


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            super.onStart(intent, startId);
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("Service", "null");
                this.stopSelf();
            } else {

                //System.out.println("Service is on...........");
                if (!CommonUtils.isHotspotEnabled(BackgroundServiceHotspotCheck.this) && Constants.hotspotstayOn) {

                    wifiApManager.setWifiApEnabled(null, true);  //Hotspot enabled
                    //AppConstants.colorToastBigFont(BackgroundServiceHotspotCheck.this, "Connecting to hotspot, please wait", Color.RED);
                    System.out.println("Connecting to hotspot, please wait....");
                }

            }

        } catch (NullPointerException e) {
            System.out.println(e);
        }
        return Service.START_STICKY;
    }
}
