package com.TrakEngineering.FluidSecureHubFOBapp.server;

import android.os.AsyncTask;

import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 6/2/2016.
 */
public class ConnectivityCheckTask extends AsyncTask<Void, Void, Void> {

    public boolean isConnected;
    public String webUrl = null;

    public ConnectivityCheckTask(String webUrl) {
        this.webUrl = webUrl;
    }

    @Override
    protected Void doInBackground(Void... voids) {


        try {
            URL myUrl = new URL(webUrl);
            URLConnection connection = myUrl.openConnection();
            connection.setConnectTimeout(2000);
            connection.connect();
            isConnected= true;
        } catch (Exception e) {
            // Handle your exceptions
            isConnected= false;
        }
        return null;
    }
}
