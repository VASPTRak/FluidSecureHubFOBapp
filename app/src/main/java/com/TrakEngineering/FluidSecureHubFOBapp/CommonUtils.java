package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.TrakEngineering.FluidSecureHubFOBapp.enity.AuthEntityClass;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.UserInfoEntity;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by VASP-LAP on 08-09-2015.
 */
public class CommonUtils {
    private static String TAG = "CommonUtils";
    private static File mypath; /*'---------------------------------------------------------------------------------------- Implemet logger functionality here....*/

    public static void LogMessage(String TAG, String TheMessage, Exception ex) {
        String logmessage = getTodaysDateInString();
        try {
            File logFileFolder = new File(Constants.LogPath);
            if (!logFileFolder.exists()) logFileFolder.mkdirs(); /*Delete file if it is more than 7 days old*/
            String OldFileToDelete = logFileFolder + "/Log_" + GetDateString(System.currentTimeMillis() - 604800000) + ".txt";
            File fd = new File(OldFileToDelete);
            if (fd.exists()) {
                fd.delete();
            }
            String LogFileName = logFileFolder + "/Log_" + GetDateString(System.currentTimeMillis()) + ".txt"; /*if(!new File(LogFileName).exists()) { new File(LogFileName).createNewFile(); }*/

            if (!new File(LogFileName).exists()) {
                File newFile = new File(LogFileName);
                newFile.createNewFile();
            }

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LogFileName, true)));
            logmessage = logmessage + " - " + TheMessage;
            if (ex != null) logmessage = logmessage + TAG + ":" + ex.getMessage();
            out.println(logmessage);
            out.close();
        } catch (Exception e1) {
            logmessage = logmessage + e1.getMessage();
            Log.d(TAG, logmessage);
        }
    }

    public static String getTodaysDateInString() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String CurrantDate = df.format(c.getTime());
        return (CurrantDate);
    }


    public static String GetDateString(Long dateinms) {
        try {
            Time myDate = new Time();
            myDate.set(dateinms);
            return myDate.format("%Y-%m-%d");
        } catch (Exception e1) {
            return "";
        }
    } // Create logger functionality

    //----------------------------------------------------------------------------

    public static void showCustomMessageDilaog(final Activity context, String title, String message) {

        final Dialog dialogBus = new Dialog(context);
        dialogBus.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialogBus.setTitle("KavachGPS Would Like to Access Your Details");
        dialogBus.setCancelable(false);
        dialogBus.setContentView(R.layout.custom_alertdialouge);
        dialogBus.show();

        TextView edt_message = (TextView) dialogBus.findViewById(R.id.edt_message);
        Button btnAllow = (Button) dialogBus.findViewById(R.id.btnAllow);
        edt_message.setText(message);

        btnAllow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBus.dismiss();

                //editVehicleNumber.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);


            }
        });

    }

    public static void showMessageDilaog(final Activity context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title

        //alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public static void showMessageDilaogFinish(final Activity context, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        context.finish();
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }


    public static void showNoInternetDialog(final Activity context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        // set title
        alertDialogBuilder.setTitle("No Internet");
        alertDialogBuilder
                .setMessage(Html.fromHtml(context.getResources().getString(R.string.no_internet)))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        context.finish();
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }


    public static void setMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());


        Class[] cArg = new Class[2];
        cArg[0] = String.class;
        cArg[1] = Boolean.TYPE;
        Method setMobileDataEnabledMethod;

        setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", cArg);

        Object[] pArg = new Object[2];
        pArg[0] = context.getPackageName();
        pArg[1] = false;

        setMobileDataEnabledMethod.setAccessible(true);

        setMobileDataEnabledMethod.invoke(connectivityManager, pArg);
    }


    public static Boolean isMobileDataEnabled(Activity activity) {
        Object connectivityService = activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager cm = (ConnectivityManager) connectivityService;

        try {
            Class<?> c = Class.forName(cm.getClass().getName());
            Method m = c.getDeclaredMethod("getMobileDataEnabled");
            m.setAccessible(true);
            return (Boolean) m.invoke(cm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isWiFiEnabled(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(WIFI_SERVICE);

        return wifiManager.isWifiEnabled();

    }

    public static boolean isHotspotEnabled(Context ctx) {

        final WifiManager wifiManager = (WifiManager) ctx.getSystemService(WIFI_SERVICE);
        final int apState;
        try {
            apState = (Integer) wifiManager.getClass().getMethod("getWifiApState").invoke(wifiManager);
            if (apState == 13) {
                return true;  // hotspot Enabled
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void SaveDataInPref(Activity activity, String data, String valueType) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.PREF_COLUMN_SITE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(valueType, data);
        editor.commit();
    }

    public static void SaveUserInPref(Activity activity, String userName, String userMobile, String userEmail, String IsOdoMeterRequire,
              String IsDepartmentRequire,String IsPersonnelPINRequire,String IsOtherRequire,String IsHoursRequire,String OtherLabel,String TimeOut,String HubId,String IsPersonnelPINRequireForHub ) {

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(AppConstants.USER_NAME, userName);
        editor.putString(AppConstants.USER_MOBILE, userMobile);
        editor.putString(AppConstants.USER_EMAIL, userEmail);
        editor.putString(AppConstants.IsOdoMeterRequire, IsOdoMeterRequire);
        editor.putString(AppConstants.IsDepartmentRequire, IsDepartmentRequire);
        editor.putString(AppConstants.IsPersonnelPINRequire, IsPersonnelPINRequire);
        //editor.putString(AppConstants.IsPersonnelPINRequireForHub, IsPersonnelPINRequireForHub);
        editor.putString(AppConstants.IsOtherRequire, IsOtherRequire);
        editor.putString(AppConstants.IsHoursRequire, IsHoursRequire);
        editor.putString(AppConstants.OtherLabel, OtherLabel);
        editor.putString(AppConstants.TimeOut, TimeOut);
        editor.putString(AppConstants.HubId, HubId);
        editor.putString(AppConstants.IsPersonnelPINRequireForHub, IsPersonnelPINRequireForHub);
        editor.commit();
    }

    public static AuthEntityClass getWiFiDetails(Activity activity, String wifiSSID) {


        AuthEntityClass authEntityClass = new AuthEntityClass();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        String dataSite = sharedPref.getString(Constants.PREF_COLUMN_SITE, "");


        try {
            if (dataSite != null) {
                JSONArray jsonArray = new JSONArray(dataSite);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Gson gson = new Gson();
                    authEntityClass = gson.fromJson(jsonObject.toString(), AuthEntityClass.class);

                }
            }
        } catch (Exception ex) {

            CommonUtils.LogMessage(TAG, "", ex);
        }

        return authEntityClass;

    }

    public static String getVersionCode(Context ctx) {

        String versioncode = "";
        try {
            PackageInfo pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            versioncode = pInfo.versionName;

        } catch (Exception q) {
            System.out.println(q);
        }

        return versioncode;
    }

    public static UserInfoEntity getCustomerDetails(Activity activity) {

        UserInfoEntity userInfoEntity = new UserInfoEntity();

        SharedPreferences sharedPref = activity.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        userInfoEntity.PersonName = sharedPref.getString(AppConstants.USER_NAME, "");
        userInfoEntity.PhoneNumber = sharedPref.getString(AppConstants.USER_MOBILE, "");
        userInfoEntity.PersonEmail = sharedPref.getString(AppConstants.USER_EMAIL, "");


        return userInfoEntity;
    }


    /**
     * Creates a hexadecimal <code>String</code> representation of the
     * <code>byte[]</code> passed. Each element is converted to a
     * <code>String</code> via the {@link Integer#toHexString(int)} and
     * separated by <code>" "</code>. If the array is <code>null</code>, then
     * <code>""<code> is returned.
     *
     * @param array
     *            the <code>byte</code> array to convert.
     * @return the <code>String</code> representation of <code>array</code> in
     *         hexadecimal.
     */
    public static String toHexString(byte[] array) {

        String bufferString = "";

        if (array != null) {
            for (int i = 0; i < array.length; i++) {
                String hexChar = Integer.toHexString(array[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }
                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }
        return bufferString;
    }

    private static boolean isHexNumber(byte value) {
        return !(!(value >= '0' && value <= '9') && !(value >= 'A' && value <= 'F')
                && !(value >= 'a' && value <= 'f'));
    }

    private static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[] { src0 }))
                .byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[] { src1 }))
                .byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }


    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString the HEX string.
     * @return the byte array.
     */
    public static byte[] toByteArray(String hexString) {

        byte[] byteArray = null;
        int count = 0;
        char c = 0;
        int i = 0;

        boolean first = true;
        int length = 0;
        int value = 0;

        // Count number of hex characters
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f') {
                count++;
            }
        }

        byteArray = new byte[(count + 1) / 2];
        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if (c >= '0' && c <= '9') {
                value = c - '0';
            } else if (c >= 'A' && c <= 'F') {
                value = c - 'A' + 10;
            } else if (c >= 'a' && c <= 'f') {
                value = c - 'a' + 10;
            } else {
                value = -1;
            }

            if (value >= 0) {

                if (first) {

                    byteArray[length] = (byte) (value << 4);

                } else {

                    byteArray[length] |= value;
                    length++;
                }

                first = !first;
            }
        }

        return byteArray;
    }



}
