package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by Administrator on 6/19/2017.
 */

public class AcceptServiceCall {

    public Activity activity;
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";
    private ConnectionDetector cd;

    public void checkAllFields() {


        String pinNumber = "";
        String vehicleNumber = "";
        String DeptNumber = "";
        String accOther = "";
        String CONNECTED_SSID = "";
        int accOdoMeter;
        int accHours;


            pinNumber = Constants.AccPersonnelPIN;
            vehicleNumber = Constants.AccVehicleNumber;
            DeptNumber = Constants.AccDepartmentNumber;
            accOther = Constants.AccOther;
            accOdoMeter = Constants.AccOdoMeter;
            accHours = Constants.AccHours;
        CONNECTED_SSID = AppConstants.LAST_CONNECTED_SSID;

        try {

            AuthEntityClass authEntityClass = new AuthEntityClass();

            authEntityClass.VehicleNumber = vehicleNumber;
            authEntityClass.FOBNumber = AppConstants.FOB_KEY_VEHICLE;
            authEntityClass.IMEIUDID = AppConstants.getIMEI(activity);
            authEntityClass.WifiSSId = CONNECTED_SSID;
            authEntityClass.SiteId = Integer.parseInt(AcceptVehicleActivity.SITE_ID);

            authEntityClass.OdoMeter = accOdoMeter;
            authEntityClass.Hours = accHours;
            authEntityClass.DepartmentNumber = DeptNumber;
            authEntityClass.PersonnelPIN = pinNumber; //Constants.AccPersonnelPIN //Check which Fs is selected
            authEntityClass.Other = accOther;
            authEntityClass.RequestFrom = "A";
            authEntityClass.RequestFromAPP ="AP";
            authEntityClass.HubId = AppConstants.HUB_ID;

            authEntityClass.CurrentLat = "" + Constants.Latitude;
            authEntityClass.CurrentLng = "" + Constants.Longitude;


            authEntityClass.AppInfo =  " Version " + CommonUtils.getVersionCode(activity) + " "+ AppConstants.getDeviceName().toLowerCase() + " " ;


            cd = new ConnectionDetector(activity);
            if (cd.isConnectingToInternet()) {


                AuthTestAsynTask authTestAsynTask = new AuthTestAsynTask(authEntityClass);
                authTestAsynTask.execute();
                authTestAsynTask.get();

                String serverRes = authTestAsynTask.response;

                if (serverRes != null) {


                    JSONObject jsonObject = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject.getString("ResponceMessage");


                    if (ResponceMessage.equalsIgnoreCase("success")) {

                        //OnHose Selection

                        String ResponceData = jsonObject.getString("ResponceData");

                        JSONObject jsonObjectRD = new JSONObject(ResponceData);

                        String TransactionId = jsonObjectRD.getString("TransactionId");
                        String VehicleId = jsonObjectRD.getString("VehicleId");
                        String PhoneNumber = jsonObjectRD.getString("PhoneNumber");
                        String PersonId = jsonObjectRD.getString("PersonId");
                        String PulseRatio = jsonObjectRD.getString("PulseRatio");
                        String MinLimit = jsonObjectRD.getString("MinLimit");
                        String FuelTypeId = jsonObjectRD.getString("FuelTypeId");
                        String ServerDate = jsonObjectRD.getString("ServerDate");
                        String IntervalToStopFuel = jsonObjectRD.getString("PumpOffTime");
                        String PrintDate = CommonUtils.getTodaysDateInStringPrint(ServerDate);
                        String Company = jsonObjectRD.getString("Company");
                        String CurrentString = jsonObjectRD.getString("Location");
                        String Location = SplitLocation(CurrentString);
                        String PersonName = jsonObjectRD.getString("PersonName");
                        String PrinterMacAddress = jsonObjectRD.getString("PrinterMacAddress");
                        String PrinterName = jsonObjectRD.getString("PrinterName");
                        AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName;
                        AppConstants.PrinterMacAddress = PrinterMacAddress;
                        System.out.println("iiiiii" + IntervalToStopFuel);

                        CommonUtils.SaveVehiFuelInPref(activity, TransactionId, VehicleId, PhoneNumber, PersonId, PulseRatio, MinLimit, FuelTypeId, ServerDate, IntervalToStopFuel,PrintDate,Company,Location,PersonName,PrinterMacAddress,PrinterName,vehicleNumber,accOther);

                        /*Intent intent = new Intent(activity, DisplayMeterActivity.class);
                        intent.putExtra(Constants.VEHICLE_NUMBER, Constants.AccVehicleNumber);
                        intent.putExtra(Constants.ODO_METER, Constants.AccOdoMeter);
                        intent.putExtra(Constants.DEPT, Constants.AccDepartmentNumber);
                        intent.putExtra(Constants.PPIN, Constants.AccPersonnelPIN);
                        intent.putExtra(Constants.OTHERR, Constants.AccOther);
                        intent.putExtra(Constants.HOURSS, Constants.AccHours);

                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        */
                        Intent intent = new Intent(activity, WelcomeActivity.class);
                        activity.startActivity(intent);

                    } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                        String ResponceText = jsonObject.getString("ResponceText");
                        String ValidationFailFor = jsonObject.getString("ValidationFailFor");
                        //CommonUtils.showMessageDilaog(activity, "Message", ResponceText);

                        AppConstants.colorToastBigFont(activity, ResponceText, Color.RED);

                        if (ValidationFailFor.equalsIgnoreCase("Vehicle")) {


                            ActivityHandler.removeActivity(2);
                            ActivityHandler.removeActivity(3);
                            ActivityHandler.removeActivity(4);
                            ActivityHandler.removeActivity(5);


                            //Intent intent = new Intent(activity, AcceptVehicleActivity.class);
                            //intent.putExtra(Constants.VEHICLE_NUMBER, Constants.AccVehicleNumber);
                            //activity.startActivity(intent);


                        } else if (ValidationFailFor.equalsIgnoreCase("Odo")) {

                            ActivityHandler.removeActivity(3);
                            ActivityHandler.removeActivity(4);
                            ActivityHandler.removeActivity(5);

                        } else if (ValidationFailFor.equalsIgnoreCase("Dept")) {

                            ActivityHandler.removeActivity(4);
                            ActivityHandler.removeActivity(5);

                        } else if (ValidationFailFor.equalsIgnoreCase("Pin")) {

                            ActivityHandler.removeActivity(5);
                        }

                    }

                } else {
                    CommonUtils.showNoInternetDialog(activity);
                }
            } else
                AppConstants.colorToast(activity, "Please check Internet Connection.", Color.RED);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public String SplitLocation(String CurrentString) {

        String LocationStr = "";
        if (!CurrentString.equalsIgnoreCase("")) {
            String[] separated = CurrentString.split(",");
            String L1 = separated[0];
            String L2 = separated[1];
            String L3 = separated[2];

            LocationStr = L1 + "," + L2 + "," + L3 + ".";

        }


        return LocationStr;
    }

    public class AuthTestAsynTask extends AsyncTask<Void, Void, Void> {

        public String response = null;
        AuthEntityClass authEntityClass = null;

        public AuthTestAsynTask(AuthEntityClass authEntityClass) {
            this.authEntityClass = authEntityClass;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();

                Gson gson = new Gson();
                String jsonData = gson.toJson(authEntityClass);
                String userEmail = CommonUtils.getCustomerDetails(activity).Email;


                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(authEntityClass.IMEIUDID + ":" + userEmail + ":" + "AuthorizationSequence");
                response = serverHandler.PostTextData(activity, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage("", "AuthTestAsynTask ", ex);
                ex.printStackTrace();
            }
            return null;
        }

    }


}
