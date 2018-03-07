package com.TrakEngineering.FluidSecureHubFOBapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {

	private Context _context;

	public ConnectionDetector(Context context){
		this._context = context;
	}

	/**
	 * Checking for all possible internet providers
	 * **/
	public boolean isConnectingToInternet(){
		boolean isConnected=false;

		ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null)
		{

			NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
			isConnected = activeNetwork != null &&	activeNetwork.isConnectedOrConnecting();
		}
		return isConnected;
	}

	/*
	public boolean isConnectedToServer() throws ExecutionException, InterruptedException {

		ConnectivityCheckTask connectivityCheckTask=new ConnectivityCheckTask(AppConstants.webURL);
		connectivityCheckTask.execute();
		connectivityCheckTask.get();
		return connectivityCheckTask.isConnected;

	}
	*/
}
