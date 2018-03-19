package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Administrator on 5/19/2016.
 */
public class AppConstants {


    public static final String DEVICE_TYPE = "A";
    public static final String USER_NAME = "userName";
    public static final String USER_MOBILE = "userMobile";
    public static final String USER_EMAIL = "userEmail";
    public static final String IsOdoMeterRequire = "IsOdoMeterRequire";
    public static final String IsDepartmentRequire = "IsDepartmentRequire";
    public static final String IsPersonnelPINRequire = "IsPersonnelPINRequire";
    public static final String IsPersonnelPINRequireForHub = "IsPersonnelPINRequireForHub";
    public static final String IsOtherRequire = "IsOtherRequire";
    public static final String IsHoursRequire = "IsHoursRequire";
    public static final String OtherLabel = "OtherLabel";
    public static final String TimeOut = "TimeOut";
    public static final String HubId = "HubId";


    public static String webIP = "http://103.8.126.241:89/";//test
    // public static String webIP = "http://fluidsecure.cloudapp.net/"; //live
    //public static String webIP = "http://103.8.126.241:93/";//New link for FS_AP
    //public static String webIP = "http://103.8.126.241:8988/";//new for hotspot changes


    public static String APDU_FOB_KEY = "";
    public static String FS_selected;
    public static String BLUETOOTH_PRINTER_NAME;
    public static String PrinterMacAddress;
    public static String BT_READER_NAME;
    public static String PulserTimingAdjust;

    public static String webURL = webIP + "HandlerTrak.ashx";
    public static String LoginURL = webIP + "LoginHandler.ashx";


    public static String Title = "";
    public static String HubName;
    public static String HubGeneratedpassword;
    public static String Login_Email;
    public static String Login_IMEI;

    public static String RES_MESSAGE = "ResponceMessage";
    public static String RES_DATA = "ResponceData";
    public static String RES_DATA_SSID = "SSIDDataObj";
    public static String RES_DATA_USER = "objUserData";
    public static String RES_TEXT = "ResponceText";

    public static String FOB_KEY_PERSON = "";
    public static String FOB_KEY_VEHICLE = "";
    public static String HUB_ID = "";


    public static String FS1_CONNECTED_SSID;
    public static String FS2_CONNECTED_SSID;
    public static String FS3_CONNECTED_SSID;
    public static String FS4_CONNECTED_SSID;

    public static String REPLACEBLE_WIFI_NAME_FS_ON_UPDATE_MAC;
    public static String REPLACEBLE_WIFI_NAME_FS1;
    public static String REPLACEBLE_WIFI_NAME_FS2;
    public static String REPLACEBLE_WIFI_NAME_FS3;
    public static String REPLACEBLE_WIFI_NAME_FS4;

    public static boolean NeedToRenameFS_ON_UPDATE_MAC;
    public static boolean NeedToRenameFS1;
    public static boolean NeedToRenameFS2;
    public static boolean NeedToRenameFS3;
    public static boolean NeedToRenameFS4;


    public static String REPLACEBLE_WIFI_NAME;
    public static String LAST_CONNECTED_SSID;
    public static String SELECTED_MACADDRESS;
    public static String CURRENT_SELECTED_SSID;
    public static String CURRENT_HOSE_SSID;
    public static String CURRENT_SELECTED_SITEID;
    public static String UPDATE_MACADDRESS;
    public static String R_HOSE_ID;
    public static String R_SITE_ID;


    public static String WIFI_PASSWORD = "";


    public static boolean NeedToRename;
    public static boolean BUSY_STATUS;


    public static boolean IS_WIFI_ON;
    public static boolean IS_DATA_ON;
    public static boolean IS_HOTSPOT_ON;

    public static ArrayList<HashMap<String, String>> DetailsServerSSIDList;
    public static ArrayList<HashMap<String, String>> DetailsListOfConnectedDevices;


    public static double roundNumber(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    public static String convertStingToBase64(String text) {
        String base64 = "";
        try {
            byte[] data = text.getBytes("UTF-8");
            base64 = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            System.out.println(e);
        }

        base64 = base64.replaceAll("\\n", "");

        return base64;
    }

    public static String getIMEI(Context ctx) {

        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return telephonyManager.getDeviceId();
        } catch (SecurityException ex) {
        ex.printStackTrace();
        return "";
        }
    }

    public static boolean isMobileDataAvailable(Context ctx) {

        boolean mobileDataEnabled = false;
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true);
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {

        }
        return mobileDataEnabled;
    }


    public static void AlertDialogBox(final Context ctx, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();


                    }
                }


        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(35);
    }

    public static void alertBigFinishActivity(final Activity ctx, String msg) {
        Dialog dialogObj;
        dialogObj = new Dialog(ctx);
        dialogObj.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogObj.setContentView(R.layout.dialog_alert_big_finish);
        dialogObj.setCancelable(false);

        TextView tvAlertMsg = (TextView) dialogObj.findViewById(R.id.tvAlertMsg);
        Button btnDialogOk = (Button) dialogObj.findViewById(R.id.btnDailogOk);


        tvAlertMsg.setText(msg);

        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ctx.finish();
            }
        });


        dialogObj.show();
    }

    public static void alertBigActivity(final Activity ctx, String msg) {
        final Dialog dialogObj;
        dialogObj = new Dialog(ctx);
        dialogObj.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogObj.setContentView(R.layout.dialog_alert_big_finish);
        dialogObj.setCancelable(false);

        TextView tvAlertMsg = (TextView) dialogObj.findViewById(R.id.tvAlertMsg);
        Button btnDialogOk = (Button) dialogObj.findViewById(R.id.btnDailogOk);


        tvAlertMsg.setText(msg);

        btnDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogObj.dismiss();
            }
        });


        dialogObj.show();
    }

    public static void AlertDialogFinish(final Activity ctx, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        ctx.finish();

                    }
                }

        );

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public static void colorToast(Context ctx, String msg, int colr) {
        Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(colr);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }


    public static void colorToastBigFont(Context ctx, String msg, int colr) {
        Toast toast = Toast.makeText(ctx, " " + msg + " ", Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(colr);
        toast.setGravity(Gravity.CENTER, 0, 0);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();

    }

    public static void notificationAlert(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String message = "Successfully completed Transaction.";
        String title = "FluidSecure";
        int icon = R.mipmap.ic_launcher;
        long when = System.currentTimeMillis();
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), icon);

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(icon)
                .setLargeIcon(largeIcon)
                .setWhen(when)
                .setAutoCancel(true)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);


    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    /*public static String getConnectedWiFidsdsdsd(Context ctx) {
        String wifiname = "";

        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid = info.getSSID();

            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }

            wifiname = ssid;
        }

        return wifiname;
    }*/

    public static void WriteinFile(String str) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/FSLog");

            if (!file.exists()) {
                if (file.mkdirs()) {
                    //System.out.println("Create FSLog Folder");
                } else {
                    // System.out.println("Fail to create KavachLog folder");
                }
            }

            File gpxfile = new File(file + "/Log.txt");
            if (!gpxfile.exists()) {
                gpxfile.createNewFile();
            }


            FileWriter fileWritter = new FileWriter(gpxfile, true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(Calendar.getInstance().getTime() + "--" + str + " ");
            bufferWritter.close();

        } catch (IOException e) {
            WriteinFile("WriteinFile Exception" + e);

        }
    }


    public static void ClearEdittextFielsOnBack(Context ctx) {

        Constants.AccVehicleNumber = "";
        Constants.AccOdoMeter = 0;
        Constants.AccDepartmentNumber = "";
        Constants.AccPersonnelPIN = "";
        Constants.AccOther = "";
    }

}
