package com.TrakEngineering.FluidSecureHubFOBapp;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by VASP-LAP on 03-05-2016.
 */
class Constants {

    final public static String VEHICLE_NUMBER="vehicleNumber";
    final public static String ODO_METER="Odometer";
    final public static String DEPT="dept";
    final public static String PPIN="pin";
    final public static String OTHERR="other";
    final public static String HOURSS="hours";
    final public static String DATE_FORMAT="MMM dd, yyyy"; // May 24, 2016
    final public static String TIME_FORMAT="hh:mm aa";
    public static final int CONNECTION_CODE = 111;
    public static final String SHARED_PREF_NAME = "UserInfo";
    public static final String PREF_COLUMN_USER = "UserData";
    public static final String PREF_COLUMN_SITE = "SiteData";
    public static final String PREF_VehiFuel = "SaveVehiFuelInPref";
    private static final int SERVER_PORT = 2901;
    private static final String SERVER_IP = "192.168.4.1";
    public static boolean hotspotstayOn = true;
    public static double Latitude = 0;
    public static double Longitude = 0;
    public static String CurrFsPass;
    public static String FS_1STATUS = "FREE";
    public static String FS_2STATUS = "FREE";
    public static String FS_3STATUS = "FREE";
    public static String FS_4STATUS = "FREE";
    public static String FS_1Gallons = "";
    public static String FS_2Gallons = "";
    public static String FS_3Gallons = "";
    public static String FS_4Gallons = "";
    public static String FS_1Pulse = "";
    public static String FS_2Pulse = "";
    public static String FS_3Pulse = "";
    public static String FS_4Pulse = "";
    public static String CurrentSelectedHose;
    public static String AccPersonnelPIN_FS1;
    public static String AccVehicleNumber_FS1;
    public static String AccDepartmentNumber_FS1;
    public static String AccOther_FS1;
    public static int AccOdoMeter_FS1=0;
    public static int AccHours_FS1;
    public static String AccVehicleNumber;
    public static String AccDepartmentNumber;
    public static String AccPersonnelPIN;
    public static String AccOther;
    public static int AccOdoMeter;
    public static int AccHours;
    //For fs number 3
    public static String AccPersonnelPIN_FS3;
    public static String AccVehicleNumber_FS3;
    public static String AccDepartmentNumber_FS3;
    public static String AccOther_FS3;
    public static int AccOdoMeter_FS3=0;
    public static int AccHours_FS3;
    //ForFs number 4
    public static String AccPersonnelPIN_FS4;
    public static String AccVehicleNumber_FS4;
    public static String AccDepartmentNumber_FS4;
    public static String AccOther_FS4;
    public static int AccOdoMeter_FS4=0;
    public static int AccHours_FS4;
    static List<String> BusyVehicleNumberList = new ArrayList<String>();
    private static String exrSdDir = Environment.getExternalStorageDirectory() + File.separator;
    private static String logFolderName = "FuelSecureAP";
    public static String LogPath=exrSdDir+logFolderName+File.separator+"Logs";
}
