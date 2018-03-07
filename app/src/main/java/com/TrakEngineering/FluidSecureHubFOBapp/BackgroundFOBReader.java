package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;

import java.io.UnsupportedEncodingException;

/**
 * Created by Administrator on 29-01-2018.
 */

public class BackgroundFOBReader extends Service{

    private BluetoothReader mBluetoothReader;
    /* ACS Bluetooth reader library. */
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;
    private ProgressDialog mProgressDialog;
    /* Bluetooth GATT client. */
    private BluetoothGatt mBluetoothGatt;

    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";
    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private static final String DEFAULT_1255_APDU_COMMAND = "FF CA 00 00 00";
    private static final String DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 48 04";
    private static final byte[] AUTO_POLLING_START = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x01};
    private static final byte[] AUTO_POLLING_STOP = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x00};


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("BackgroundFOBReader**************");

    /* Initialize BluetoothReaderGattCallback. */
        mGattCallback = new BluetoothReaderGattCallback();

        /* Register BluetoothReaderGattCallback's listeners */
        mGattCallback
                .setOnConnectionStateChangeListener(new BluetoothReaderGattCallback.OnConnectionStateChangeListener() {

                    @Override
                    public void onConnectionStateChange(
                            final BluetoothGatt gatt, final int state,
                            final int newState) {


                                if (state != BluetoothGatt.GATT_SUCCESS) {
                                    /*
                                     * Show the message on fail to
                                     * connect/disconnect.
                                     */
                                    if (newState == BluetoothReader.STATE_CONNECTED) {
                                        // mTxtConnectionState.setText(R.string.connect_fail);



                                        AppConstants.colorToastBigFont(getApplicationContext(),"Bluetooth is Connected", Color.RED);

                                    } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                                        // mTxtConnectionState.setText(R.string.disconnect_fail);

                                        AppConstants.colorToastBigFont(getApplicationContext(),"Bluetooth is disconnected\nReconnecting...",Color.RED);
                                        System.out.println("Bluetooth is disconnectedBluetooth is disconnected");

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                                              disconnectReader();

                                        //connectReader();




                                    }


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
                            Toast.makeText(BackgroundFOBReader.this,
                                    "The device is not supported!",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mBluetoothReader = reader;
                        setListener(reader);
                        activateReader(reader);
                    }
                });


        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void disconnectReader() {

        System.out.println("BT NFC disconnected...........");

        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void Authentation() {

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

    public void Startpolling() {


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

    public void TransmitApdu() {

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

    private void setListener(BluetoothReader reader) {

        /* Wait for authentication completed. */
        mBluetoothReader
                .setOnAuthenticationCompleteListener(new BluetoothReader.OnAuthenticationCompleteListener() {

                    @Override
                    public void onAuthenticationComplete(
                            BluetoothReader bluetoothReader, final int errorCode) {


                                if (errorCode == BluetoothReader.ERROR_SUCCESS) {

                                    System.out.println("Authentication success " + errorCode);

                                    Startpolling();//Start polling

                                } else {
                                    System.out.println("Authentication Failed!! " + errorCode);
                                }

                    }

                });

        /* Wait for receiving ATR string. */
        mBluetoothReader
                .setOnAtrAvailableListener(new BluetoothReader.OnAtrAvailableListener() {

                    @Override
                    public void onAtrAvailable(BluetoothReader bluetoothReader,
                                               final byte[] atr, final int errorCode) {
                        if (atr == null) {
                            System.out.println("result APDU " + errorCode);
                        } else {
                            System.out.println("result APDU " + errorCode);
                        }
                    }

                });

        /* Wait for response APDU. */
        mBluetoothReader
                .setOnResponseApduAvailableListener(new BluetoothReader.OnResponseApduAvailableListener() {

                    @Override
                    public void onResponseApduAvailable(
                            BluetoothReader bluetoothReader, final byte[] apdu,
                            final int errorCode) {
                        System.out.println("result APDU " + getResponseString(apdu, errorCode));
                        String FobKey = getResponseString(apdu, errorCode);
                        if (FobKey.length() > 6) {
                            AppConstants.APDU_FOB_KEY = FobKey;
                        }
                    }

                });

        mBluetoothReader
                .setOnCardStatusChangeListener(new BluetoothReader.OnCardStatusChangeListener() {

                    @Override
                    public void onCardStatusChange(
                            BluetoothReader bluetoothReader, final int sta) {

                        String CardStatus = getCardStatusString(sta);
                        if (CardStatus.equalsIgnoreCase("Present.")) {
                            AppConstants.APDU_FOB_KEY = "";
                            TransmitApdu();
                            transmitEscapeCommend();

                        } else if (CardStatus.equalsIgnoreCase("Power saving mode.")) {

                        }
                    }

                });



    }

    /* Start the process to enable the reader's notifications. */
    private void activateReader(BluetoothReader reader) {
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

    private String getResponseString(byte[] response, int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            if (response != null && response.length > 0) {
                return CommonUtils.toHexString(response);
            }
            return "";
        }
        return getErrorString(errorCode);
    }
    private String getErrorString(int errorCode) {
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

    private String getCardStatusString(int cardStatus) {
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

    public void PowerOnCard() {

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



}
