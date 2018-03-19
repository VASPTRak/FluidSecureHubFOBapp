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

    private ConnectionDetector cd;
    public Activity activity;
    String IsOdoMeterRequire = "", IsDepartmentRequire = "", IsPersonnelPINRequire = "", IsOtherRequire = "";



    public void checkAllFields() {


        String pinNumber = "";
        String vehicleNumber = "";
        String DeptNumber = "";
        String accOther = "";
        String CONNECTED_SSID = "";
        int accOdoMeter;
        int accHours;

        if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS1"))
        {
            pinNumber = Constants.AccPersonnelPIN_FS1;
            vehicleNumber = Constants.AccVehicleNumber_FS1;
            DeptNumber = Constants.AccDepartmentNumber_FS1;
            accOther = Constants.AccOther_FS1;
            accOdoMeter = Constants.AccOdoMeter_FS1;
            accHours = Constants.AccHours_FS1;
            CONNECTED_SSID = AppConstants.FS1_CONNECTED_SSID;

        }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS2")){
            pinNumber = Constants.AccPersonnelPIN;
            vehicleNumber = Constants.AccVehicleNumber;
            DeptNumber = Constants.AccDepartmentNumber;
            accOther = Constants.AccOther;
            accOdoMeter = Constants.AccOdoMeter;
            accHours = Constants.AccHours;
            CONNECTED_SSID = AppConstants.FS2_CONNECTED_SSID;
        }else if (Constants.CurrentSelectedHose.equalsIgnoreCase("FS3")){
            pinNumber = Constants.AccPersonnelPIN_FS3;
            vehicleNumber = Constants.AccVehicleNumber_FS3;
            DeptNumber = Constants.AccDepartmentNumber_FS3;
            accOther = Constants.AccOther_FS3;
            accOdoMeter = Constants.AccOdoMeter_FS3;
            accHours = Constants.AccHours_FS3;
            CONNECTED_SSID = AppConstants.FS3_CONNECTED_SSID;
        }else{
            pinNumber = Constants.AccPersonnelPIN_FS4;
            vehicleNumber = Constants.AccVehicleNumber_FS4;
            DeptNumber = Constants.AccDepartmentNumber_FS4;
            accOther = Constants.AccOther_FS4;
            accOdoMeter = Constants.AccOdoMeter_FS4;
            accHours = Constants.AccHours_FS4;
            CONNECTED_SSID = AppConstants.FS4_CONNECTED_SSID;
        }

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
                        if (Constants.CurrentSelectedHose.equals("FS1")) {
/*

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            String TransactionId_FS1 = jsonObjectRD.getString("TransactionId");
                            String VehicleId_FS1 = jsonObjectRD.getString("VehicleId");
                            String PhoneNumber_FS1 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS1 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS1 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS1 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS1 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS1 = jsonObjectRD.getString("ServerDate");
                            String IntervalToStopFuel_FS1 = jsonObjectRD.getString("PumpOffTime");
                            String PrintDate_FS1 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS1);

                            String Company_FS1 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS1 = SplitLocation(CurrentString);
                            String PersonName_FS1 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS1 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS1 = jsonObjectRD.getString("PrinterName");
                            AppConstants.PrinterMacAddress = PrinterMacAddress_FS1;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS1;
                            System.out.println("iiiiii" + IntervalToStopFuel_FS1);

                            CommonUtils.SaveVehiFuelInPref_FS1(activity, TransactionId_FS1,VehicleId_FS1, PhoneNumber_FS1, PersonId_FS1, PulseRatio_FS1, MinLimit_FS1, FuelTypeId_FS1, ServerDate_FS1, IntervalToStopFuel_FS1,PrintDate_FS1,Company_FS1,Location_FS1,PersonName_FS1,PrinterMacAddress_FS1,PrinterName_FS1,vehicleNumber,accOther);


                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.AccVehicleNumber_FS1);
                            intent.putExtra(Constants.ODO_METER, Constants.AccOdoMeter_FS1);
                            intent.putExtra(Constants.DEPT, Constants.AccDepartmentNumber_FS1);
                            intent.putExtra(Constants.PPIN, Constants.AccPersonnelPIN_FS1);
                            intent.putExtra(Constants.OTHERR, Constants.AccOther_FS1);
                            intent.putExtra(Constants.HOURSS, Constants.AccHours_FS1);

                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);

                        } else if (Constants.CurrentSelectedHose.equals("FS2")){

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

                        Intent intent = new Intent(activity, DisplayMeterActivity.class);
                        intent.putExtra(Constants.VEHICLE_NUMBER, Constants.AccVehicleNumber);
                        intent.putExtra(Constants.ODO_METER, Constants.AccOdoMeter);
                        intent.putExtra(Constants.DEPT, Constants.AccDepartmentNumber);
                        intent.putExtra(Constants.PPIN, Constants.AccPersonnelPIN);
                        intent.putExtra(Constants.OTHERR, Constants.AccOther);
                        intent.putExtra(Constants.HOURSS, Constants.AccHours);

                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);

                        }
                        else if (Constants.CurrentSelectedHose.equals("FS3")) {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            String TransactionId_FS3 = jsonObjectRD.getString("TransactionId");
                            String VehicleId_FS3 = jsonObjectRD.getString("VehicleId");
                            String PhoneNumber_FS3 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS3 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS3 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS3 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS3 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS3 = jsonObjectRD.getString("ServerDate");
                            String IntervalToStopFuel_FS3 = jsonObjectRD.getString("PumpOffTime");
                            String PrintDate_FS3 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS3);
                            String Company_FS3 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS3 = SplitLocation(CurrentString);
                            String PersonName_FS3 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS3 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS3 = jsonObjectRD.getString("PrinterName");
                            AppConstants.PrinterMacAddress = PrinterMacAddress_FS3;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS3;
                            System.out.println("iiiiii" + IntervalToStopFuel_FS3);

                            CommonUtils.SaveVehiFuelInPref_FS3(activity, TransactionId_FS3,VehicleId_FS3, PhoneNumber_FS3, PersonId_FS3, PulseRatio_FS3, MinLimit_FS3, FuelTypeId_FS3, ServerDate_FS3, IntervalToStopFuel_FS3,PrintDate_FS3,Company_FS3,Location_FS3,PersonName_FS3,PrinterMacAddress_FS3,PrinterName_FS3,vehicleNumber,accOther);


                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.AccVehicleNumber_FS3);
                            intent.putExtra(Constants.ODO_METER, Constants.AccOdoMeter_FS3);
                            intent.putExtra(Constants.DEPT, Constants.AccDepartmentNumber_FS3);
                            intent.putExtra(Constants.PPIN, Constants.AccPersonnelPIN_FS3);
                            intent.putExtra(Constants.OTHERR, Constants.AccOther_FS3);
                            intent.putExtra(Constants.HOURSS, Constants.AccHours_FS3);

                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);

                        }else {

                            String ResponceData = jsonObject.getString("ResponceData");

                            JSONObject jsonObjectRD = new JSONObject(ResponceData);

                            String TransactionId_FS4 = jsonObjectRD.getString("TransactionId");
                            String VehicleId_FS4 = jsonObjectRD.getString("VehicleId");
                            String PhoneNumber_FS4 = jsonObjectRD.getString("PhoneNumber");
                            String PersonId_FS4 = jsonObjectRD.getString("PersonId");
                            String PulseRatio_FS4 = jsonObjectRD.getString("PulseRatio");
                            String MinLimit_FS4 = jsonObjectRD.getString("MinLimit");
                            String FuelTypeId_FS4 = jsonObjectRD.getString("FuelTypeId");
                            String ServerDate_FS4 = jsonObjectRD.getString("ServerDate");
                            String IntervalToStopFuel_FS4 = jsonObjectRD.getString("PumpOffTime");
                            String PrintDate_FS4 = CommonUtils.getTodaysDateInStringPrint(ServerDate_FS4);
                            String Company_FS4 = jsonObjectRD.getString("Company");
                            String CurrentString = jsonObjectRD.getString("Location");
                            String Location_FS4 = SplitLocation(CurrentString);
                            String PersonName_FS4 = jsonObjectRD.getString("PersonName");
                            String PrinterMacAddress_FS4 = jsonObjectRD.getString("PrinterMacAddress");
                            String PrinterName_FS4 = jsonObjectRD.getString("PrinterName");
                            AppConstants.PrinterMacAddress = PrinterMacAddress_FS4;
                            AppConstants.BLUETOOTH_PRINTER_NAME = PrinterName_FS4;
                            System.out.println("iiiiii" + IntervalToStopFuel_FS4);


                            CommonUtils.SaveVehiFuelInPref_FS4(activity, TransactionId_FS4,VehicleId_FS4, PhoneNumber_FS4, PersonId_FS4, PulseRatio_FS4, MinLimit_FS4, FuelTypeId_FS4, ServerDate_FS4, IntervalToStopFuel_FS4,PrintDate_FS4,Company_FS4,Location_FS4,PersonName_FS4,PrinterMacAddress_FS4,PrinterName_FS4,vehicleNumber,accOther);


                            Intent intent = new Intent(activity, DisplayMeterActivity.class);
                            intent.putExtra(Constants.VEHICLE_NUMBER, Constants.AccVehicleNumber_FS4);
                            intent.putExtra(Constants.ODO_METER, Constants.AccOdoMeter_FS4);
                            intent.putExtra(Constants.DEPT, Constants.AccDepartmentNumber_FS4);
                            intent.putExtra(Constants.PPIN, Constants.AccPersonnelPIN_FS4);
                            intent.putExtra(Constants.OTHERR, Constants.AccOther_FS4);
                            intent.putExtra(Constants.HOURSS, Constants.AccHours_FS4);

                            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            activity.startActivity(intent);
*/
                        }


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
        }
    }


    public class AuthTestAsynTask extends AsyncTask<Void, Void, Void> {

        AuthEntityClass authEntityClass = null;


        public String response = null;

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
            }
            return null;
        }

    }

    public String SplitLocation(String CurrentString)
    {

        String LocationStr = "";
        if (!CurrentString.equalsIgnoreCase(""))
        {
            String[] separated = CurrentString.split(",");
            String L1 = separated[0];
            String L2 = separated[1];
            String L3 = separated[2];

            LocationStr = L1+","+L2+","+L3+".";

        }


        return LocationStr;
    }


}
