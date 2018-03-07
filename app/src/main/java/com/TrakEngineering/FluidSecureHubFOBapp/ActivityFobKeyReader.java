package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.acs.audiojack.AudioJackReader;
import com.acs.audiojack.DukptReceiver;
import com.acs.audiojack.Result;

import java.security.GeneralSecurityException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ActivityFobKeyReader extends AppCompatActivity {

    Button btn_reset, btn_power_on, btn_transmit_malual, btn_back, btn_retry;


    public AudioManager mAudioManager;
    public static AudioJackReader mReader;
    private DukptReceiver mDukptReceiver = new DukptReceiver();
    private Context mContext = this;

    private ProgressDialog mProgress;
    private Object mResponseEvent = new Object();


    private boolean mResultReady;

    private boolean mPiccAtrReady;
    private byte[] mPiccAtr;

    private boolean mPiccResponseApduReady;
    private byte[] mPiccResponseApdu;

    private byte[] mIksn = new byte[10];
    private byte[] mIpek = new byte[16];
    private int mPiccTimeout;
    private int mPiccCardType;
    private byte[] mPiccCommandApdu;
    private byte[] mPiccRfConfig = new byte[19];

    private final BroadcastReceiver mHeadsetPlugReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {

                boolean plugged = (intent.getIntExtra("state", 0) == 1);

                /* Mute the audio output if the reader is unplugged. */
                mReader.setMute(!plugged);
            }
        }
    };


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fobkey_reader);
        //addPreferencesFromResource(R.xml.preferences);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReader = new AudioJackReader(mAudioManager, true);

        /* Register the headset plug receiver. */
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetPlugReceiver, filter);

        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_power_on = (Button) findViewById(R.id.btn_power_on);
        btn_transmit_malual = (Button) findViewById(R.id.btn_transmit_malual);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_retry = (Button) findViewById(R.id.btn_retry);

        //Disale back bUtton
        btn_back.setEnabled(false);

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            /* Show the progress. */
                mProgress.setMessage("Resetting the reader...");
                mProgress.show();

            /* Reset the reader. */
                mReader.reset(new OnResetCompleteListener());
            }
        });

        btn_power_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 /* Check the reset volume. */
                if (!checkResetVolume()) {

                }

            /* Clear the ATR. */
                //mPiccAtrPreference.setSummary("");

            /* Show the progress. */
                mProgress.setMessage("Powering on the PICC...");
                mProgress.show();

                new Thread(new PowerOn()).start();
            }
        });


        btn_transmit_malual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  /* Check the reset volume. */
                if (!checkResetVolume()) {

                }

            /* Clear the response APDU. */
                // mPiccResponseApduPreference.setSummary("");

            /* Show the progress. */
                mProgress.setMessage("Transmitting the command APDU...");
                mProgress.show();

                new Thread(new Transmit()).start();

            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AppConstants.FOB_KEY_VEHICLE = "";
                Intent intent = new Intent(ActivityFobKeyReader.this, AcceptVehicleActivity.class);
                finish();
                startActivity(intent);
            }
        });

        btn_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btn_back.setEnabled(false);
                for (int i = 0; i < 2; i++) {
                    AppConstants.colorToastBigFont(ActivityFobKeyReader.this, "Please place fob key on reader", Color.BLUE);
                }

                //Check Volume and reset command
                checkResetVolume();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

               /* Show the progress. */
                        mProgress.setMessage("Resetting the reader...");
                        mProgress.show();

                /* Reset the reader. */
                        mReader.reset(new OnResetCompleteListener());


                    }
                }, 3000);


           /*     //Set power ON
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                  *//*Check the reset volume. *//*
                        if (!checkResetVolume()) {

                        }


             *//*Show the progress. *//*
                        mProgress.setMessage("Powering on the PICC...");
                        mProgress.show();

                        new Thread(new PowerOn()).start();

                    }
                }, 7000);

                //transmit Command
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        *//*  Show the progress. *//*
                        mProgress.setMessage("Transmitting the command APDU...");
                        mProgress.show();

                        new Thread(new Transmit()).start();
                    }
                }, 12000);*/


            }
        });

        /* Load the PICC timeout. */
        String piccTimeoutString = "5"; //

        // mPiccTimeoutPreference.getText();
        if ((piccTimeoutString == null) || piccTimeoutString.equals("")) {
            piccTimeoutString = "5";
        }
        try {
            mPiccTimeout = Integer.parseInt(piccTimeoutString);
        } catch (NumberFormatException e) {
            mPiccTimeout = 5;
        }
        piccTimeoutString = Integer.toString(mPiccTimeout);
       /* Load the PICC card type. */
        String piccCardTypeString = "8F";//mPiccCardTypePreference.getText();
        if ((piccCardTypeString == null) || piccCardTypeString.equals("")) {
            piccCardTypeString = "8F";
        }
        byte[] cardType = new byte[1];
        toByteArray(piccCardTypeString, cardType);
        mPiccCardType = cardType[0] & 0xFF;
        piccCardTypeString = toHexString(mPiccCardType);

        /* Load the PICC command APDU. */
        String piccCommandApduString = "FF CA 00 00 00";//mPiccCommandApduPreference.getText();
        if ((piccCommandApduString == null)
                || (piccCommandApduString.equals(""))) {
            piccCommandApduString = "FF CA 00 00 00";
        }
        mPiccCommandApdu = toByteArray(piccCommandApduString);
        piccCommandApduString = toHexString(mPiccCommandApdu);
        //mPiccCommandApduPreference.setText(piccCommandApduString);
        //mPiccCommandApduPreference.setSummary(piccCommandApduString);

        /* Load the PICC RF configuration. */
        String piccRfConfigString = "07 85 85 85 85 85 85 85 85 69 69 69 69 69 69 69 69 3F 3F";//mPiccRfConfigPreference.getText();
        if ((piccRfConfigString == null) || piccRfConfigString.equals("")
                || (toByteArray(piccRfConfigString, mPiccRfConfig) != 19)) {

            piccRfConfigString = "07 85 85 85 85 85 85 85 85 69 69 69 69 69 69 69 69 3F 3F";
            toByteArray(piccRfConfigString, mPiccRfConfig);
        }
        piccRfConfigString = toHexString(mPiccRfConfig);


        /* Initialize the progress dialog */
        mProgress = new ProgressDialog(mContext);
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);


        /* Set the PICC ATR callback. */
        mReader.setOnPiccAtrAvailableListener(new OnPiccAtrAvailableListener());

        /* Set the PICC response APDU callback. */
        mReader.setOnPiccResponseApduAvailableListener(new OnPiccResponseApduAvailableListener());

        /* Set the key serial number. */
        mDukptReceiver.setKeySerialNumber(mIksn);

        /* Load the initial key. */
        mDukptReceiver.loadInitialKey(mIpek);

        //for (int i = 0; i < 2; i++) {}
            AppConstants.colorToastBigFont(this, "Please place fob key on reader", Color.BLUE);

       /* new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            *//* Show the progress. *//*
                mProgress.setMessage("Resetting the reader...");
                mProgress.show();

        *//* Reset the reader. *//*
           mReader.reset(new OnResetCompleteListener());


            }
        }, 500);*/


   /*     //Set power ON
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

            *//* Show the progress. *//*
                mProgress.setMessage("Powering on the PICC...");
                mProgress.show();

                new Thread(new PowerOn()).start();

            }
        }, 1000);*/

       /* //transmit Command
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                         *//* Show the progress. *//*
                mProgress.setMessage("Transmitting the command APDU...");
                mProgress.show();

                new Thread(new Transmit()).start();
            }
        }, 12000);*/


    }


    private class OnResetCompleteListener implements
            AudioJackReader.OnResetCompleteListener {

        @Override
        public void onResetComplete(AudioJackReader reader) {

                /* Hide the progress. */
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mProgress.dismiss();
                    //Set power ON
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            //Show the progress.
                                    mProgress.setMessage("Powering on the PICC...");
                            mProgress.show();

                            new Thread(new PowerOn()).start();

                        }
                    }, 1000);
                }

                ;
            });
        }
    }


    private class OnPiccAtrAvailableListener implements
            AudioJackReader.OnPiccAtrAvailableListener {

        @Override
        public void onPiccAtrAvailable(AudioJackReader reader, byte[] atr) {

            synchronized (mResponseEvent) {

                /* Store the PICC ATR. */
                mPiccAtr = new byte[atr.length];
                System.arraycopy(atr, 0, mPiccAtr, 0, atr.length);

                /* Trigger the response event. */
                mPiccAtrReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }

    private class OnPiccResponseApduAvailableListener implements
            AudioJackReader.OnPiccResponseApduAvailableListener {

        @Override
        public void onPiccResponseApduAvailable(AudioJackReader reader,
                                                byte[] responseApdu) {

            synchronized (mResponseEvent) {

                /* Store the PICC response APDU. */
                mPiccResponseApdu = new byte[responseApdu.length];
                System.arraycopy(responseApdu, 0, mPiccResponseApdu, 0,
                        responseApdu.length);

                /* Trigger the response event. */
                mPiccResponseApduReady = true;
                mResponseEvent.notifyAll();
            }
        }
    }


    @Override
    protected void onDestroy() {

        /* Unregister the headset plug receiver. */
        unregisterReceiver(mHeadsetPlugReceiver);

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mReader.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppConstants.FOB_KEY_VEHICLE = "";
        mReader.start();

        checkResetVolume();


    }

    @Override
    protected void onPause() {
        super.onPause();
        mProgress.dismiss();
        mReader.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgress.dismiss();
        mReader.stop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean ret = true;

        switch (item.getItemId()) {

            case R.id.action_clear:
                clearData();
                break;

            case R.id.action_about:
                showVersionInfo();
                break;

            default:
                ret = super.onOptionsItemSelected(item);
                break;
        }

        return ret;
    }


    /**
     * Converts the error code to string.
     *
     * @param errorCode the error code.
     * @return the error code string.
     */
    private String toErrorCodeString(int errorCode) {

        String errorCodeString = null;

        switch (errorCode) {
            case Result.ERROR_SUCCESS:
                errorCodeString = "The operation completed successfully.";
                break;
            case Result.ERROR_INVALID_COMMAND:
                errorCodeString = "The command is invalid.";
                break;
            case Result.ERROR_INVALID_PARAMETER:
                errorCodeString = "The parameter is invalid.";
                break;
            case Result.ERROR_INVALID_CHECKSUM:
                errorCodeString = "The checksum is invalid.";
                break;
            case Result.ERROR_INVALID_START_BYTE:
                errorCodeString = "The start byte is invalid.";
                break;
            case Result.ERROR_UNKNOWN:
                errorCodeString = "The error is unknown.";
                break;
            case Result.ERROR_DUKPT_OPERATION_CEASED:
                errorCodeString = "The DUKPT operation is ceased.";
                break;
            case Result.ERROR_DUKPT_DATA_CORRUPTED:
                errorCodeString = "The DUKPT data is corrupted.";
                break;
            case Result.ERROR_FLASH_DATA_CORRUPTED:
                errorCodeString = "The flash data is corrupted.";
                break;
            case Result.ERROR_VERIFICATION_FAILED:
                errorCodeString = "The verification is failed.";
                break;
            case Result.ERROR_PICC_NO_CARD:
                errorCodeString = "No card in PICC slot.";
                break;
            default:
                errorCodeString = "Error communicating with reader.";
                break;
        }

        return errorCodeString;
    }


    /**
     * Converts the byte array to HEX string.
     *
     * @param buffer the buffer.
     * @return the HEX string.
     */
    private String toHexString(byte[] buffer) {

        String bufferString = "";

        if (buffer != null) {

            for (int i = 0; i < buffer.length; i++) {

                String hexChar = Integer.toHexString(buffer[i] & 0xFF);
                if (hexChar.length() == 1) {
                    hexChar = "0" + hexChar;
                }

                bufferString += hexChar.toUpperCase(Locale.US) + " ";
            }
        }

        return bufferString;
    }

    /**
     * Converts the integer to HEX string.
     *
     * @param i the integer.
     * @return the HEX string.
     */
    private String toHexString(int i) {

        String hexString = Integer.toHexString(i);

        if (hexString.length() % 2 == 1) {
            hexString = "0" + hexString;
        }

        return hexString.toUpperCase(Locale.US);
    }

    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString the HEX string.
     * @return the number of bytes.
     */
    private int toByteArray(String hexString, byte[] byteArray) {

        char c = 0;
        boolean first = true;
        int length = 0;
        int value = 0;
        int i = 0;

        for (i = 0; i < hexString.length(); i++) {

            c = hexString.charAt(i);
            if ((c >= '0') && (c <= '9')) {
                value = c - '0';
            } else if ((c >= 'A') && (c <= 'F')) {
                value = c - 'A' + 10;
            } else if ((c >= 'a') && (c <= 'f')) {
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

            if (length >= byteArray.length) {
                break;
            }
        }

        return length;
    }

    /**
     * Converts the HEX string to byte array.
     *
     * @param hexString the HEX string.
     * @return the byte array.
     */
    private byte[] toByteArray(String hexString) {

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

    /**
     * Decrypts the data using AES.
     *
     * @param key   the key.
     * @param input the input buffer.
     * @return the output buffer.
     * @throws GeneralSecurityException if there is an error in the decryption process.
     */
    private byte[] aesDecrypt(byte key[], byte[] input)
            throws GeneralSecurityException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        return cipher.doFinal(input);
    }

    /**
     * Decrypts the data using Triple DES.
     *
     * @param key   the key.
     * @param input the input buffer.
     * @return the output buffer.
     * @throws GeneralSecurityException if there is an error in the decryption process.
     */
    private byte[] tripleDesDecrypt(byte[] key, byte[] input)
            throws GeneralSecurityException {

        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[8]);

        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(input);
    }

    /**
     * Clears the data.
     */
    private void clearData() {


    }

    /**
     * Shows the version information.
     */
    private void showVersionInfo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        PackageInfo packageInfo = null;
        String version = null;

        try {

            /* Get the version name. */
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),
                    0);
            version = getString(R.string.version) + " "
                    + packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {

            version = getString(R.string.unknown_version);
        }

        builder.setMessage(version)
                .setTitle(getString(R.string.action_about))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });

        builder.show();
    }

    /**
     * Shows the message dialog.
     *
     * @param titleId   the title ID.
     * @param messageId the message ID.
     */
    private void showMessageDialog(int titleId, int messageId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage(messageId)
                .setTitle(titleId)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });

        builder.show();
    }

    /**
     * Checks the reset volume.
     *
     * @return true if current volume is equal to maximum volume.
     */
    private boolean checkResetVolume() {

        boolean ret = true;

        int currentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);

        int maxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (currentVolume < maxVolume) {

            showMessageDialog(R.string.info, R.string.message_reset_info_volume);
            ret = false;
        }

        return ret;
    }


    /**
     * Shows the request queue error.
     */
    private void showRequestQueueError() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                /* Show the request queue error. */
                Toast.makeText(mContext, "The request cannot be queued.",
                        Toast.LENGTH_LONG).show();
                btn_back.setEnabled(true);
            }
        });
    }


    /**
     * Shows the PICC ATR.
     */
    private void showPiccAtr() {

        synchronized (mResponseEvent) {

            /* Wait for the PICC ATR. */
            while (!mPiccAtrReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mPiccAtrReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the PICC ATR. */
                        //mPiccAtrPreference.setSummary(toHexString(mPiccAtr));

                        System.out.println("toHexString(mPiccAtr)--" + toHexString(mPiccAtr));
                        /* Show the progress. */
                        mProgress.setMessage("Transmitting the command APDU...");
                        mProgress.show();

                        new Thread(new Transmit()).start();
                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        //Show the result.
                       /* Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();*/
                        btn_back.setEnabled(true);
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        AppConstants.FOB_KEY_VEHICLE = "";
                        //AppConstants.colorToastBigFont(mContext, "The operation timed out. Please retry..", Color.RED);
                        Toast.makeText(getApplication(), "The operation timed out. Please retry..", Toast.LENGTH_SHORT).show();
                        btn_back.setEnabled(true);
                    }
                });
            }

            mPiccAtrReady = false;
            mResultReady = false;
        }
    }

    /**
     * Shows the PICC response APDU.
     */
    private void showPiccResponseApdu() {

        synchronized (mResponseEvent) {

            /* Wait for the PICC response APDU. */
            while (!mPiccResponseApduReady && !mResultReady) {

                try {
                    mResponseEvent.wait(10000);
                } catch (InterruptedException e) {
                }

                break;
            }

            if (mPiccResponseApduReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the PICC response APDU. */
                        //mPiccResponseApduPreference.setSummary(toHexString(mPiccResponseApdu));
                        String FobKey = toHexString(mPiccResponseApdu);
                        System.out.println("Response: " + mPiccResponseApdu);
                        System.out.println("Response_length: " + mPiccResponseApdu.length);
                        System.out.println("Response: " + toHexString(mPiccResponseApdu));
                        if (FobKey.length() > 6) {
                            AppConstants.FOB_KEY_VEHICLE = toHexString(mPiccResponseApdu);
                            Toast.makeText(getApplicationContext(), "Response success:  " + toHexString(mPiccResponseApdu), Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(ActivityFobKeyReader.this, AcceptVehicleActivity.class);
                            finish();
                            startActivity(intent);
                        } else {
                            AppConstants.FOB_KEY_VEHICLE = "";
                            AppConstants.colorToastBigFont(mContext, "Something went wrong. Please retry..", Color.RED);
                            btn_back.setEnabled(true);

                        }


                    }
                });

            } else if (mResultReady) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the result. */
                        /*Toast.makeText(mContext,
                                toErrorCodeString(mResult.getErrorCode()),
                                Toast.LENGTH_LONG).show();*/
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        /* Show the timeout. */
                        AppConstants.FOB_KEY_VEHICLE = "";
                        AppConstants.colorToastBigFont(mContext, "The operation timed out. Please retry..", Color.RED);
                        btn_back.setEnabled(true);
                    }
                });
            }

            mPiccResponseApduReady = false;
            mResultReady = false;
        }
    }

    private class Transmit implements Runnable {

        @Override
        public void run() {

                /* Transmit the command APDU. */
            mPiccResponseApduReady = false;
            mResultReady = false;
            if (!mReader.piccTransmit(mPiccTimeout, mPiccCommandApdu)) {

                    /* Show the request queue error. */
                showRequestQueueError();

            } else {

                    /* Show the PICC response APDU. */
                showPiccResponseApdu();
            }

                /* Hide the progress. */
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mProgress.dismiss();
                }

                ;
            });
        }
    }

    private class PowerOn implements Runnable {

        @Override
        public void run() {

                /* Power on the PICC. */
            mPiccAtrReady = false;
            mResultReady = false;
            if (!mReader.piccPowerOn(mPiccTimeout, mPiccCardType)) {

             /* Show the request queue error. */
                showRequestQueueError();

            } else {

               /* Show the PICC ATR. */
                showPiccAtr();
            }

                /* Hide the progress. */
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mProgress.dismiss();
                }

                ;
            });
        }
    }


}
