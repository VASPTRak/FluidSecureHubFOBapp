package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;

import java.io.UnsupportedEncodingException;

import static com.google.android.gms.internal.zzid.runOnUiThread;

/**
 * Created by root on 1/9/18.
 */

public class BluetoothReaderReciver extends BroadcastReceiver{

    /* Default master key. */
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";
    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private static final String DEFAULT_1255_APDU_COMMAND = "FF CA 00 00 00";
    private static final String DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 48 04";
    private static final byte[] AUTO_POLLING_START = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x01};
    private static final byte[] AUTO_POLLING_STOP = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x00};
    public static Context ctx;
    /* Reader to be connected. */
    private static String mDeviceName;
    private static String mDeviceAddress;

    /* Detected reader. */
    private static BluetoothReader mBluetoothReader;
    /* ACS Bluetooth reader library. */
    private static BluetoothReaderManager mBluetoothReaderManager;
    private static BluetoothReaderGattCallback mGattCallback;
    private ProgressDialog mProgressDialog;
    /* Bluetooth GATT client. */
    private static BluetoothGatt mBluetoothGatt;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;
        
        BluetoothAdapter bluetoothAdapter = null;
        BluetoothManager bluetoothManager = null;
        final String action = intent.getAction();

        if (!(mBluetoothReader instanceof Acr3901us1Reader)) {
                /* Only ACR3901U-S1 require bonding. */
            return;
        }

        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            Log.i("A_pin Btreader", "ACTION_BOND_STATE_CHANGED");

                /* Get bond (pairing) state */
            if (mBluetoothReaderManager == null) {
                Log.w("A_pin Btreader", "Unable to initialize BluetoothReaderManager.");
                return;
            }

            bluetoothManager = (BluetoothManager) ctx. getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.w("A_pin Btreader", "Unable to initialize BluetoothManager.");
                return;
            }

            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                Log.w("A_pin Btreader", "Unable to initialize BluetoothAdapter.");
                return;
            }

            final BluetoothDevice device = bluetoothAdapter
                    .getRemoteDevice(mDeviceAddress);

            if (device == null) {
                return;
            }

            final int bondState = device.getBondState();

            // TODO: remove log message
            Log.i("A_pin Btreader", "BroadcastReceiver - getBondState. state = "
                    + getBondingStatusString(bondState));

                /* Enable notification */
            if (bondState == BluetoothDevice.BOND_BONDED) {
                if (mBluetoothReader != null) {
                    mBluetoothReader.enableNotification(true);
                }
            }

                /* Progress Dialog */
            if (bondState == BluetoothDevice.BOND_BONDING) {
                mProgressDialog = ProgressDialog.show(context,
                        "ACR3901U-S1", "Bonding...");
            } else {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            }
        }

    }


    /*
   * Update listener
   */
    private static void setListener(BluetoothReader reader) {

        /* Wait for authentication completed. */
        mBluetoothReader
                .setOnAuthenticationCompleteListener(new BluetoothReader.OnAuthenticationCompleteListener() {

                    @Override
                    public void onAuthenticationComplete(
                            BluetoothReader bluetoothReader, final int errorCode) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (errorCode == BluetoothReader.ERROR_SUCCESS) {

                                    
                                    //Toast.makeText(getApplicationContext(), "Authentication Success!", Toast.LENGTH_SHORT).show();
                                    //transmitEscapeCommend();
                                    Startpolling();//Start polling

                                } else {
                                    //Toast.makeText(getApplicationContext(), "Authentication Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                });

        /* Wait for receiving ATR string. */
        mBluetoothReader
                .setOnAtrAvailableListener(new BluetoothReader.OnAtrAvailableListener() {

                    @Override
                    public void onAtrAvailable(BluetoothReader bluetoothReader,
                                               final byte[] atr, final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (atr == null) {
                                    //Toast.makeText(getApplicationContext(), getErrorString(errorCode), Toast.LENGTH_SHORT).show();
                                    System.out.println("result APDU " + getErrorString(errorCode));
                                } else {
                                    // Toast.makeText(getApplicationContext(), CommonUtils.toHexString(atr), Toast.LENGTH_SHORT).show();
                                    System.out.println("result APDU " + getErrorString(errorCode));
                                }
                            }
                        });
                    }

                });

        /* Wait for response APDU. */
        mBluetoothReader
                .setOnResponseApduAvailableListener(new BluetoothReader.OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                System.out.println("result APDU " + getResponseString(apdu, errorCode));
                                String FobKey = getResponseString(apdu, errorCode);
 
                            }
                        });
                    }

                });

        mBluetoothReader
                .setOnCardStatusChangeListener(new BluetoothReader.OnCardStatusChangeListener() {

                    @Override
                    public void onCardStatusChange(
                            BluetoothReader bluetoothReader, final int sta) {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String CardStatus = getCardStatusString(sta);
                                if (CardStatus.equalsIgnoreCase("Present.")) {
                                    TransmitApdu();

                                } else if (CardStatus.equalsIgnoreCase("Power saving mode.")) {

                                }
                            }
                        });
                    }

                });




    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void ConnectReader(){


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        String Devicename = preferences.getString("BT_READER_DEVICE_NAME", "");
        String DevicemacAddress = preferences.getString("BT_READER_DEVICE_ADDRESS", "");

        mDeviceName = Devicename; //"ACR1255U-J1-005931";// AppConstants.BT_READER_DEVICE_NAME;
        mDeviceAddress = DevicemacAddress; //"F0:C7:7F:C6:5A:D4";// AppConstants.BT_READER_DEVICE_ADDRESS;


        //----------------Bluetooth reader------------------

          /* Initialize BluetoothReaderGattCallback. */
        mGattCallback = new BluetoothReaderGattCallback();

        /* Register BluetoothReaderGattCallback's listeners */
        mGattCallback
                .setOnConnectionStateChangeListener(new BluetoothReaderGattCallback.OnConnectionStateChangeListener() {

                    @Override
                    public void onConnectionStateChange(
                            final BluetoothGatt gatt, final int state,
                            final int newState) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (state != BluetoothGatt.GATT_SUCCESS) {
                                    /*
                                     * Show the message on fail to
                                     * connect/disconnect.
                                     */
                                    if (newState == BluetoothReader.STATE_CONNECTED) {
                                        // mTxtConnectionState.setText(R.string.connect_fail);
                                    } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                                        // mTxtConnectionState.setText(R.string.disconnect_fail);
                                    }
                                    //invalidateOptionsMenu();
                                    return;
                                }


                                if (newState == BluetoothProfile.STATE_CONNECTED) {
                                    /* Detect the connected reader. */
                                    if (mBluetoothReaderManager != null) {
                                        mBluetoothReaderManager.detectReader(
                                                gatt, mGattCallback);

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {

                                                Authentation(); //Authentation
                                            }
                                        }, 5000);

                                    }
                                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                    mBluetoothReader = null;
                                    /*
                                     * Release resources occupied by Bluetooth
                                     * GATT client.
                                     */
                                    if (mBluetoothGatt != null) {
                                        // mBluetoothGatt.close();
                                        mBluetoothGatt = null;
                                    }
                                }
                            }
                        });
                    }
                });

        /* Initialize mBluetoothReaderManager. */
        mBluetoothReaderManager = new BluetoothReaderManager();

        /* Register BluetoothReaderManager's listeners */
        mBluetoothReaderManager
                .setOnReaderDetectionListener(new BluetoothReaderManager.OnReaderDetectionListener() {

                    @Override
                    public void onReaderDetection(BluetoothReader reader) {

                        if (reader instanceof Acr3901us1Reader) {
                            /* The connected reader is ACR3901U-S1 reader. */
                            Log.v("A_pin Btreader", "On Acr3901us1Reader Detected.");
                        } else if (reader instanceof Acr1255uj1Reader) {
                            /* The connected reader is ACR1255U-J1 reader. */
                            Log.v("A_pin Btreader", "On Acr1255uj1Reader Detected.");
                        } else {
                            runOnUiThread(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                                @Override
                                public void run() {


                                    /* Disconnect Bluetooth reader */
                                    Log.v("A_pin Btreader", "Disconnect reader!!!");
                                    //disconnectReader();
                                }
                            });
                            return;
                        }

                        mBluetoothReader = reader;
                        setListener(reader);
                        activateReader(reader);
                    }
                });

        if (!mDeviceName.equals("") && !mDeviceAddress.equals("")) {

            /* Connect the reader. */
            connectReader();

        }

        //-------------------Bluttooth reader----------------------


    }



    /* Start the process to enable the reader's notifications. */
    private static void activateReader(BluetoothReader reader) {
        if (reader == null) {
            return;
        }

        if (reader instanceof Acr3901us1Reader) {
            /* Start pairing to the reader. */
            ((Acr3901us1Reader) mBluetoothReader).startBonding();
        } else if (mBluetoothReader instanceof Acr1255uj1Reader) {
            /* Enable notification. */
            mBluetoothReader.enableNotification(true);
        }
    }

    //-------------------Bluetooth Reader------------

    /*
* Create a GATT connection with the reader. And detect the connected reader
* once service list is available.
*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static boolean connectReader() {
        BluetoothManager bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.w("A_pin Btreader", "Unable to initialize BluetoothManager.");
            return false;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.w("A_pin Btreader", "Unable to obtain a BluetoothAdapter.");
            return false;
        }



        /* Create a new connection. */
        final BluetoothDevice device = bluetoothAdapter
                .getRemoteDevice(mDeviceAddress);

        if (device == null) {
            Log.w("A_pin Btreader", "Device not found. Unable to connect.");
            return false;
        }

        /* Connect to GATT server. */
        mBluetoothGatt = device.connectGatt( ctx, false, mGattCallback);
        return true;
    }

    private void getSystemService(String bluetoothService) {
    }

    /* Disconnects an established connection. */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void disconnectReader() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }


    /* Get the Bonding status string. */
    private String getBondingStatusString(int bondingStatus) {
        if (bondingStatus == BluetoothDevice.BOND_BONDED) {
            return "BOND BONDED";
        } else if (bondingStatus == BluetoothDevice.BOND_NONE) {
            return "BOND NONE";
        } else if (bondingStatus == BluetoothDevice.BOND_BONDING) {
            return "BOND BONDING";
        }
        return "BOND UNKNOWN.";
    }


    /* Get the Error string. */
    private static String getErrorString(int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            return "";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_CHECKSUM) {
            return "The checksum is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA_LENGTH) {
            return "The data length is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_COMMAND) {
            return "The command is invalid.";
        } else if (errorCode == BluetoothReader.ERROR_UNKNOWN_COMMAND_ID) {
            return "The command ID is unknown.";
        } else if (errorCode == BluetoothReader.ERROR_CARD_OPERATION) {
            return "The card operation failed.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_REQUIRED) {
            return "Authentication is required.";
        } else if (errorCode == BluetoothReader.ERROR_LOW_BATTERY) {
            return "The battery is low.";
        } else if (errorCode == BluetoothReader.ERROR_CHARACTERISTIC_NOT_FOUND) {
            return "Error characteristic is not found.";
        } else if (errorCode == BluetoothReader.ERROR_WRITE_DATA) {
            return "Write command to reader is failed.";
        } else if (errorCode == BluetoothReader.ERROR_TIMEOUT) {
            return "Timeout.";
        } else if (errorCode == BluetoothReader.ERROR_AUTHENTICATION_FAILED) {
            return "Authentication is failed.";
        } else if (errorCode == BluetoothReader.ERROR_UNDEFINED) {
            return "Undefined error.";
        } else if (errorCode == BluetoothReader.ERROR_INVALID_DATA) {
            return "Received data error.";
        } else if (errorCode == BluetoothReader.ERROR_COMMAND_FAILED) {
            return "The command failed.";
        }
        return "Unknown error.";
    }

    /* Get the Response string. */
    private static String getResponseString(byte[] response, int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            if (response != null && response.length > 0) {
                return CommonUtils.toHexString(response);
            }
            return "";
        }
        return getErrorString(errorCode);
    }

    /* Get the Card status string. */
    private static String getCardStatusString(int cardStatus) {
        if (cardStatus == BluetoothReader.CARD_STATUS_ABSENT) {
            return "Absent.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_PRESENT) {
            return "Present.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWERED) {
            return "Powered.";
        } else if (cardStatus == BluetoothReader.CARD_STATUS_POWER_SAVING_MODE) {
            PowerOnCard();//power on cardreader.
            return "Power saving mode.";
        }
        return "The card status is unknown.";
    }

    public static void PowerOnCard() {

        if (mBluetoothReader == null) {
            System.out.println("card_reader_not_ready");
            //Toast.makeText(getApplicationContext(), "card_reader_not_ready", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBluetoothReader.powerOnCard()) {
            System.out.println("card_reader_not_ready");
            //Toast.makeText(getApplicationContext(), "card_reader_not_ready", Toast.LENGTH_SHORT).show();
        }

    }

    public static void Authentation() {

        try {

        /* Retrieve master key from edit box. */
            byte masterKey[] = CommonUtils.toByteArray(CommonUtils.toHexString(DEFAULT_1255_MASTER_KEY.getBytes("UTF-8")));


            if (masterKey != null && masterKey.length > 0) {

                try {
                    if (!mBluetoothReader.authenticate(masterKey)) {
                        System.out.println("card_reader_not_ready");
                        //Toast.makeText(getApplicationContext(), "card_reader_not_ready", Toast.LENGTH_SHORT).show();

                    } else {
                        System.out.println("Authenticating..");
                        //Toast.makeText(getApplicationContext(), "Authenticating...", Toast.LENGTH_SHORT).show();

                    }
                } catch (NullPointerException e) {
                    System.out.println(e);
                }
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void Startpolling() {


        if (mBluetoothReader == null) {
            System.out.println("card_reader_not_ready");
            //Toast.makeText(getApplicationContext(), "card_reader_not_ready", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mBluetoothReader.transmitEscapeCommand(AUTO_POLLING_START)) {
            System.out.println("card_reader_not_ready");
            //Toast.makeText(getApplicationContext(), "card_reader_not_ready", Toast.LENGTH_SHORT).show();
        }

    }

    public static void TransmitApdu() {

            /* Check for detected reader. */
        if (mBluetoothReader == null) {
            System.out.println("card_reader_not_ready");
            //Toast.makeText(getApplicationContext(), "card_reader_not_ready", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Retrieve APDU command from APDU COMMAND. */
        byte apduCommand[] = CommonUtils.toByteArray(DEFAULT_1255_APDU_COMMAND);

        if (apduCommand != null && apduCommand.length > 0) {

           /* Transmit APDU command. */
            if (!mBluetoothReader.transmitApdu(apduCommand)) {
                System.out.println("card_reader_not_ready");
                //Toast.makeText(getApplicationContext(), "card_reader_not_ready", Toast.LENGTH_SHORT).show();
            }
        } else {
            System.out.println("Character format error!");
            //Toast.makeText(getApplicationContext(), "Character format error!", Toast.LENGTH_SHORT).show();
        }

    }

    public void transmitEscapeCommend(){

            /* Check for detected reader. */
        if (mBluetoothReader == null) {
            System.out.println("card_reader_not_ready");
            return;
        }

                /* Retrieve escape command from edit box. */
        byte escapeCommand[] =  CommonUtils.toByteArray(DEFAULT_1255_ESCAPE_COMMAND);

        if (escapeCommand != null && escapeCommand.length > 0) {
                    /* Clear response field for result of escape command. */
            System.out.println("No Data");

                    /* Transmit escape command. */
            if (!mBluetoothReader.transmitEscapeCommand(escapeCommand)) {
                System.out.println("card_reader_not_ready");
            }
        } else {
            System.out.println("Character format error!");
        }

    }


    //-------------------ends-----------------
}
