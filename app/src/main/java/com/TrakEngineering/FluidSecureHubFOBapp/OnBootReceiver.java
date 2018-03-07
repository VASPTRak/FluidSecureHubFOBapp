package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by User on 12/6/2017.
 */

public class OnBootReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {

        //Launch team viewer host application on reboot.
        /*Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.teamviewer.host.market");//com.teamviewer.quicksupport.market
        if (launchIntent != null) {
            context.startActivity(launchIntent);//null pointer check in case package name was not found
        }*/
    }
}