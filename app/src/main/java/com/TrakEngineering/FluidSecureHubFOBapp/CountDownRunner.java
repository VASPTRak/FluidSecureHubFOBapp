package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Activity;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by VASP-LAP on 21-09-2015.
 */
public class CountDownRunner implements Runnable {
    private static final String TAG = "CountDownRunner";
    Activity context;
    TextView textTime;
    MenuItem NetworkmenuItem;
    // @Override
    public CountDownRunner(Activity context, TextView textTime)
    {   this.NetworkmenuItem=NetworkmenuItem;
        this.context=context;
        this.textTime=textTime;
    }
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {


                doWork(context,textTime);
                Thread.sleep(1000); // Pause of 1 Second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
            }
        }
    }


    public void doWork(final Activity context, final TextView textTime) {
        context.runOnUiThread(new Runnable() {
            public void run() {
                try {


                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT+" "+Constants.TIME_FORMAT);
                    String currentDateTimeString = sdf.format(new Date());

                    // textView is the TextView view that should display it
                    textTime.setText(currentDateTimeString);

                    //CommonUtils.setBadgeCount(context);
                    //txtCurrentTime.setText(curTime);
                } catch (Exception ex) {

                    CommonUtils.LogMessage(TAG, " doWork ", ex);
                }
            }
        });
    }
    // End display time on top end here
    //-------------------------------------------------------------------------------------------
}