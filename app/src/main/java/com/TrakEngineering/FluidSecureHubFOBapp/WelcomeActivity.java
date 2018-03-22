package com.TrakEngineering.FluidSecureHubFOBapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.TrakEngineering.FluidSecureHubFOBapp.WifiHotspot.WifiApManager;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.RenameHose;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.UpdateMacAddressClass;
import com.TrakEngineering.FluidSecureHubFOBapp.enity.UserInfoEntity;
import com.TrakEngineering.FluidSecureHubFOBapp.server.ServerHandler;
import com.acs.bluetooth.Acr1255uj1Reader;
import com.acs.bluetooth.Acr3901us1Reader;
import com.acs.bluetooth.BluetoothReader;
import com.acs.bluetooth.BluetoothReaderGattCallback;
import com.acs.bluetooth.BluetoothReaderManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.TrakEngineering.FluidSecureHubFOBapp.R.id.textView;


public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    /* Stops scanning after 10 seconds. */
    private static final long SCAN_PERIOD = 3000;
    /* Default master key. */
    private static final String DEFAULT_1255_MASTER_KEY = "ACR1255U-J1 Auth";
    /* Read 16 bytes from the binary block 0x04 (MIFARE 1K or 4K). */
    private static final String DEFAULT_1255_APDU_COMMAND = "FF CA 00 00 00";
    private static final String DEFAULT_1255_ESCAPE_COMMAND = "E0 00 00 48 04";
    private static final String DEFAULT_1255_ESCAPE_COMMAND_NO_SLEEP = "E0 00 00 48 04";//NO SLEEP
    private static final byte[] AUTO_POLLING_START = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x01};
    private static final byte[] AUTO_POLLING_STOP = {(byte) 0xE0, 0x00, 0x00, 0x40, 0x00};
    static WifiApManager wifiApManager;
    private static int SelectedItemPos;
    ProgressDialog dialog1;
    Button btn_disconnect;
    TextView tv_NFS1;
    TextView tv_NFS2;
    TextView tv_NFS3;
    TextView tv_NFS4;//tv_fs1_pulse
    LinearLayout linearHose, linear_fs_1, linear_fs_2, linear_fs_3, linear_fs_4;
    String IsOdoMeterRequire = "";
    //FS For Stopbutton
    String PhoneNumber;
    String outputQuantity = "0";
    boolean stopTimer = true;
    double minFuelLimit = 0, numPulseRatio = 0;
    double fillqty = 0;
    String URL_GET_PULSAR_FS1, URL_SET_PULSAR_FS1, URL_WIFI_FS1, URL_RELAY_FS1, URL_GET_PULSAR_FS2, URL_SET_PULSAR_FS2, URL_WIFI_FS2, URL_RELAY_FS2, URL_GET_PULSAR_FS3, URL_SET_PULSAR_FS3, URL_WIFI_FS3, URL_RELAY_FS3, URL_GET_PULSAR_FS4, URL_SET_PULSAR_FS4, URL_WIFI_FS4, URL_RELAY_FS4;
    String HTTP_URL_FS_1 = "", HTTP_URL_FS_2 = "", HTTP_URL_FS_3 = "", HTTP_URL_FS_4 = "";
    String jsonRename;
    String jsonRelayOff = "{\"relay_request\":{\"Password\":\"12345678\",\"Status\":0}}";
    String jsonPulsar = "{\"pulsar_request\":{\"counter_set\":1}}";
    String jsonPulsarOff = "{\"pulsar_request\":{\"counter_set\":0}}";
    Timer ScreenOutTime;
    private TextView tvSSIDName;
    private WifiManager mainWifi;
    private StringBuilder sb = new StringBuilder();
    private ArrayList<HashMap<String, String>> serverSSIDList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> ListOfConnectedDevices = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private TextView tvLatLng;
    private boolean isTCancelled = false;
    private int RetryOneAtemptConnectToSelectedSSSID = 0;
    private String IsDepartmentRequire = "";
    private String IsPersonnelPINRequireForHub = "";
    private String IsPersonnelPINRequire = "";
    private String IsOtherRequire = "";
    private String consoleString = "";
    private ProgressDialog loading = null;
    private String HTTP_URL = "";//"http://192.168.43.153:80/";//for pipe
    private String URL_INFO = "";
    private String URL_UPDATE_FS_INFO = "";
    private Timer t;
    private String TAG = " WelcomeActivity ";
    private float density;
    private TextView textDateTime, tv_fs1_Qty, tv_fs2_Qty, tv_fs3_Qty, tv_fs4_Qty, tv_FS1_hoseName, tv_FS2_hoseName, tv_FS3_hoseName,
            tv_FS4_hoseName, tv_fs1_stop, tv_fs2_stop, tv_fs3_stop, tv_fs4_stop, tv_fs1QTN, tv_fs2QTN, tv_fs3QTN, tv_fs4QTN, tv_fs1_pulseTxt, tv_fs2_pulseTxt, tv_fs3_pulseTxt, tv_fs4_pulseTxt, tv_fs1_Pulse, tv_fs2_Pulse, tv_fs3_Pulse, tv_fs4_Pulse;
    private ImageView imgFuelLogo;
    private TextView tvTitle;
    private Button btnGo, btnRetryWifi;
    //================================================

    //---------------Bluetooth reader using Gatt------------------------
    private ConnectionDetector cd;
    private double latitude = 0;
    private double longitude = 0;
    //============Bluetooth reader Gatt==============
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    /* Reader to be connected. */
    private String mDeviceName;
    private String mDeviceAddress;

    /* Detected reader. */
    private BluetoothReader mBluetoothReader;
    /* ACS Bluetooth reader library. */
    private BluetoothReaderManager mBluetoothReaderManager;
    private BluetoothReaderGattCallback mGattCallback;
    private ProgressDialog mProgressDialog;
    /*
     * Listen to Bluetooth bond status change event. And turns on reader's
     * notifications once the card reader is bonded.
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
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

                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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

    };
    /* Bluetooth GATT client. */
    private BluetoothGatt mBluetoothGatt;
    //Phone NFC
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;
    /* Device scan callback. */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {

            try {
                if (device.getName().equalsIgnoreCase(AppConstants.BT_READER_NAME)) {

                    //Toast.makeText(getApplicationContext(), "Device found: "+AppConstants.BT_READER_NAME, Toast.LENGTH_SHORT).show();

                    AppConstants.WriteinFile("WelcomeActivity~~~~~~~~~" + "BT_READER_DEVICE_NAME " + device.getName());
                    AppConstants.WriteinFile("WelcomeActivity~~~~~~~~~" + "BT_READER_DEVICE_ADDRESS " + String.valueOf(device));
                    scanLeDevice(false);

                    mDeviceName = device.getName();
                    mDeviceAddress = String.valueOf(device);

                   /* try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            setBluetooth(false);//Off Bluetooth
                        }
                    }, 1000);


                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            setBluetooth(true);
                        }
                    }, 4000);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            connectReader();
                        }
                    }, 6000);

                    /*// Connect the reader.
                    if(connectReader()) {
                        transmitEscapeCommend();
                    }*/

                }
            } catch (NullPointerException e) {
                System.out.println(e);
            }

        }
    };

    private static String PasswordGeneration() {

        String FinalPass;
        String hubName = AppConstants.HubName;//"HUB00000001";
        String numb = hubName.substring(hubName.length() - 8);
        String numb1 = numb.substring(0, 4);
        String numb2 = hubName.substring(hubName.length() - 4);

        String result1 = "";
        String result2 = "";

        //Result one
        for (int i = 0; i < numb1.length(); i++) {

            String xp = String.valueOf(numb1.charAt(i));
            int p = Integer.parseInt(xp);

            if (p >= 5) {
                p = p - 2;
                result1 = result1 + p;

            } else {
                p = p + i + 1;
                result1 = result1 + p;
            }

        }

        //Result Two
        String rev_numb2 = new StringBuilder(numb2).reverse().toString();
        String res = "";
        for (int j = 0; j < rev_numb2.length(); j++) {

            String xps = String.valueOf(rev_numb2.charAt(j));
            int q = Integer.parseInt(xps);

            if (q >= 5) {
                q = q - 2;
                res = res + q;

            } else {
                q = q + j + 1;
                res = res + q;
            }
            result2 = new StringBuilder(res).reverse().toString();

        }
        FinalPass = "HUB" + result1 + result2;
        System.out.println("FinalPass" + FinalPass);

        return FinalPass;
    }


    //_---------------Bluetooth reader using Gatt------------------------

    private static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    /*
     * Update listener
     */
    private void setListener(BluetoothReader reader) {

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

                                    System.out.println("Authentication success " + getErrorString(errorCode));

                                    Startpolling();//Start polling

                                } else {
                                    System.out.println("Authentication Failed!! " + getErrorString(errorCode));
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
                                    System.out.println("result APDU " + getErrorString(errorCode));
                                } else {
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
                                if (FobKey.length() > 6) {
                                    AppConstants.APDU_FOB_KEY = FobKey;
                                }

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
                                    AppConstants.APDU_FOB_KEY = "";
                                    TransmitApdu();
                                    transmitNOSLEEPCommand();

                                } else if (CardStatus.equalsIgnoreCase("Power saving mode.")) {

                                }
                            }
                        });
                    }

                });

        mBluetoothReader
                .setOnEnableNotificationCompleteListener(new BluetoothReader.OnEnableNotificationCompleteListener() {

                    @Override
                    public void onEnableNotificationComplete(
                            BluetoothReader bluetoothReader, final int result) {
                        if (result != BluetoothGatt.GATT_SUCCESS) {
                                    /* Fail */
                            //Toast.makeText(getApplicationContext(),"The device is unable to set notification!", Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(getApplicationContext(),"The device is ready to use!",Toast.LENGTH_SHORT).show();

                           // startService(new Intent(WelcomeActivity.this,BackgroundFOBReader.class));
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();

        //Hide keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

/*
        linear_fs_1.setVisibility(View.INVISIBLE);
        linear_fs_2.setVisibility(View.INVISIBLE);
        linear_fs_3.setVisibility(View.INVISIBLE);
        linear_fs_4.setVisibility(View.INVISIBLE);
*/
        ReconnectFobReader();//Reconnect Fobreader by recreating welcome activity
        new GetConnectedDevicesIP().execute();
        new GetSSIDUsingLocationOnResume().execute();
        UpdateFSUI_seconds();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }



    }

    /*//Is a tag discovered?
    @Override
    public void onNewIntent(Intent intent) {
        System.out.println("saw intent");
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent)){
            System.out.println("saw nfc");
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            byte[] id = tag.getId();
            String FobKey= CommonUtils.toHex(id);
            System.out.println("read nfc " + FobKey);
            if (FobKey.length() > 6) {
                AppConstants.APDU_FOB_KEY = FobKey;
            }
        }
    }*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onPause() {
        super.onPause();
        t.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        t.cancel();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
//        tvSSIDName = (TextView) findViewById(R.id.tvSSIDName);
        tvLatLng = (TextView) findViewById(R.id.tvLatLng);

        tvLatLng.setVisibility(View.GONE);
        mHandler = new Handler();
        SelectedItemPos = -1;

        getSupportActionBar().setTitle(R.string.fs_name);
        getSupportActionBar().setIcon(R.drawable.fuel_secure_lock);

        density = getResources().getDisplayMetrics().density;

        Button btn_disconnect = (Button) findViewById(R.id.btn_disconnect);
        TextView tvVersionNum = (TextView) findViewById(R.id.tvVersionNum);
        tvVersionNum.setText("Version " + CommonUtils.getVersionCode(WelcomeActivity.this));


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();


        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectReader();
                Toast.makeText(WelcomeActivity.this, "Reader disconnected", Toast.LENGTH_SHORT).show();
            }
        });

        InItGUI();

//        mDeviceName = "ACR1255U-J1-005931";// AppConstants.BT_READER_DEVICE_NAME;
//        mDeviceAddress = "F0:C7:7F:C6:5A:D4";// AppConstants.BT_READER_DEVICE_ADDRESS;


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

                                        AppConstants.colorToastBigFont(getApplicationContext(),"Bluetooth is Connected",Color.RED);

                                    } else if (newState == BluetoothReader.STATE_DISCONNECTED) {
                                        // mTxtConnectionState.setText(R.string.disconnect_fail);

                                        AppConstants.colorToastBigFont(getApplicationContext(),"Bluetooth is disconnected\nReconnecting...",Color.RED);
                                        System.out.println("Bluetooth is disconnectedBluetooth is disconnected");

                                        //disconnectReader();

                                        //connectReader();


                                       // startService(new Intent(WelcomeActivity.this, BackgroundFOBReader.class));



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
                                    Toast.makeText(WelcomeActivity.this,
                                            "The device is not supported!",
                                            Toast.LENGTH_SHORT).show();

                                    /* Disconnect Bluetooth reader */
                                    Log.v("A_pin Btreader", "Disconnect reader!!!");

                                }
                            });
                            return;
                        }

                        mBluetoothReader = reader;
                        setListener(reader);
                        activateReader(reader);


                    }
                });


        //-------------------Bluttooth reader----------------------

        new GetConnectedDevicesIP().execute(); //getListOfConnectedDevice();

        //Enable Background service to check hotspot
        EnableHotspotBackgService();

        // set User Information
        UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

        AppConstants.Title = "Hub name : " + userInfoEntity.PersonName;//+ "\nMobile : " + userInfoEntity.PhoneNumber + "\nEmail : " + userInfoEntity.PersonEmail
        AppConstants.HubName = userInfoEntity.PersonName;
        tvTitle = (TextView) findViewById(textView);
        tvTitle.setText(AppConstants.Title);
        wifiApManager = new WifiApManager(this);

        //Set Hotspot name and password
        setHotspotNamePassword(this);

        // Display current date time u
        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner(this, textDateTime);
        myThread = new Thread(myRunnableThread);
        myThread.start();
        //end current date time----------------------------------------------

        if (ActivityHandler.screenStack != null)
            ActivityHandler.screenStack.clear();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (AppConstants.BUSY_STATUS)
                    new ChangeBusyStatus().execute();

                String mobDevName = AppConstants.getDeviceName().toLowerCase();
                System.out.println("oooooooooo" + mobDevName);
                if (mobDevName.contains("moto") && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    DBController controller = new DBController(WelcomeActivity.this);
                    ArrayList<HashMap<String, String>> uData = controller.getAllTransaction();

                    if (uData != null && uData.size() > 0) {
                        startService(new Intent(WelcomeActivity.this, BackgroundService.class));
                        System.out.println("BackgroundService Start...");
                    } else {
                        stopService(new Intent(WelcomeActivity.this, BackgroundService.class));
                        System.out.println("BackgroundService STOP...");
                    }
                }
            }
        }, 2000);


        btnRetryWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppConstants.colorToastBigFont(getApplicationContext(),"Please wait for few seconds....",Color.BLUE);
                new WiFiConnectTask().execute();
            }
        });

        //SW 3/6 Phone NFC
        //mAdapter = NfcAdapter.getDefaultAdapter(this);
        // mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        //mAdapter.enableForegroundNdefPush(this, mNdefPushMessage);


    }

    private void EnableHotspotBackgService() {


        Calendar cal = Calendar.getInstance();
        Intent name = new Intent(WelcomeActivity.this, BackgroundServiceHotspotCheck.class);
        PendingIntent pintent = PendingIntent.getService(getApplicationContext(), 0, name, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60000, pintent);
        //scan and enable hotspot if OFF
        Constants.hotspotstayOn = true;

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (loading != null) {
            loading.dismiss();
            Constants.hotspotstayOn = true;
            loading = null;
        }
    }

    private void UpdateFSUI_seconds() {

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // System.out.println("FS UI Update here");
                                int FS_Count = serverSSIDList.size();
                                if (!serverSSIDList.isEmpty()) {
/*
                                    //FS Visibility on Dashboard
                                    if (FS_Count == 1) {
                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));

                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.INVISIBLE);
                                        linear_fs_3.setVisibility(View.INVISIBLE);
                                        linear_fs_4.setVisibility(View.INVISIBLE);

                                    } else if (FS_Count == 2) {


                                        //------------
                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));

                                        // System.out.println("MacAddress" + serverSSIDList.get(0).get("MacAddress").toString());


                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);

                                        linear_fs_3.setVisibility(View.INVISIBLE);
                                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linear_fs_3.getLayoutParams();
                                        params.height = 0; // In dp
                                        linear_fs_3.setLayoutParams(params);

                                        linear_fs_4.setVisibility(View.INVISIBLE);
                                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) linear_fs_4.getLayoutParams();
                                        params1.height = 0; // In dp
                                        linear_fs_4.setLayoutParams(params1);

                                    } else if (FS_Count == 3) {

                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        tv_FS3_hoseName.setText(serverSSIDList.get(2).get("WifiSSId"));


                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);

                                        linear_fs_3.setVisibility(View.VISIBLE);
                                       /* LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linear_fs_3.getLayoutParams();
                                        params.height = match_parent; // In dp
                                        linear_fs_3.setLayoutParams(params);*/

                                    // linear_fs_4.setVisibility(View.INVISIBLE);
                                       /* LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) linear_fs_4.getLayoutParams();
                                        params1.height = 0; // In dp
                                        linear_fs_4.setLayoutParams(params1);*/
/*

                                    } else {

                                        tv_FS1_hoseName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                        tv_FS2_hoseName.setText(serverSSIDList.get(1).get("WifiSSId"));
                                        tv_FS3_hoseName.setText(serverSSIDList.get(2).get("WifiSSId"));
                                        tv_FS4_hoseName.setText(serverSSIDList.get(3).get("WifiSSId"));

                                        linear_fs_1.setVisibility(View.VISIBLE);
                                        linear_fs_2.setVisibility(View.VISIBLE);

                                        linear_fs_3.setVisibility(View.VISIBLE);
                                        /*LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linear_fs_3.getLayoutParams();
                                        params.height = match_parent; // In dp
                                        linear_fs_3.setLayoutParams(params);*/

                                    // linear_fs_4.setVisibility(View.VISIBLE);
                                        /*LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) linear_fs_4.getLayoutParams();
                                        params1.height = match_parent; // In dp
                                        linear_fs_4.setLayoutParams(params1);

                                    }*/
                                }

                                //===Display Dashboard every Second=====
                                // DisplayDashboardEveSecond();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.first, menu);//Menu Resource, Menu
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mClose:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    disconnectReader();
                }
                 finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            System.out.println("rrr" + String.valueOf(mLastLocation.getLatitude()));
            System.out.println("rrr" + String.valueOf(mLastLocation.getLongitude()));


            LocationManager locationManager = (LocationManager) WelcomeActivity.this.getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


            if (!statusOfGPS) {
                latitude = 0;
                longitude = 0;
            } else {
                latitude = mLastLocation.getLatitude();
                // AcceptVehicleActivity.CurrentLat = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                // AcceptVehicleActivity.CurrentLng = mLastLocation.getLongitude();
            }

            if (latitude == 0 && longitude == 0) {
                AppConstants.AlertDialogFinish(WelcomeActivity.this, "Unable to get current location.\nPlease try again later!");
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void InItGUI() {

        textDateTime = (TextView) findViewById(R.id.textDateTime);/*
        tv_fs1_Qty = (TextView) findViewById(R.id.tv_fs1_Qty);
        tv_fs2_Qty = (TextView) findViewById(R.id.tv_fs2_Qty);
        tv_fs3_Qty = (TextView) findViewById(R.id.tv_fs3_Qty);
        tv_fs4_Qty = (TextView) findViewById(R.id.tv_fs4_Qty);
        tv_FS2_hoseName = (TextView) findViewById(R.id.tv_FS2_hoseName);
        tv_FS1_hoseName = (TextView) findViewById(R.id.tv_FS1_hoseName);
        tv_FS3_hoseName = (TextView) findViewById(R.id.tv_FS3_hoseName);
        tv_FS4_hoseName = (TextView) findViewById(R.id.tv_FS4_hoseName);

        tv_fs1_pulseTxt = (TextView) findViewById(R.id.tv_fs1_pulseTxt);
        tv_fs2_pulseTxt = (TextView) findViewById(R.id.tv_fs2_pulseTxt);
        tv_fs3_pulseTxt = (TextView) findViewById(R.id.tv_fs3_pulseTxt);
        tv_fs4_pulseTxt = (TextView) findViewById(R.id.tv_fs4_pulseTxt);

        tv_fs1_Pulse = (TextView) findViewById(R.id.tv_fs1_Pulse);
        tv_fs2_Pulse = (TextView) findViewById(R.id.tv_fs2_Pulse);
        tv_fs3_Pulse = (TextView) findViewById(R.id.tv_fs3_Pulse);
        tv_fs4_Pulse = (TextView) findViewById(R.id.tv_fs4_Pulse);

        tv_fs1_stop = (TextView) findViewById(R.id.tv_fs1_stop);
        tv_fs2_stop = (TextView) findViewById(R.id.tv_fs2_stop);
        tv_fs3_stop = (TextView) findViewById(R.id.tv_fs3_stop);
        tv_fs4_stop = (TextView) findViewById(R.id.tv_fs4_stop);

        tv_NFS1 = (TextView) findViewById(R.id.tv_NFS1);
        tv_NFS2 = (TextView) findViewById(R.id.tv_NFS2);
        tv_NFS3 = (TextView) findViewById(R.id.tv_NFS3);
        tv_NFS4 = (TextView) findViewById(R.id.tv_NFS4);

        tv_fs1QTN = (TextView) findViewById(R.id.tv_fs1QTN);
        tv_fs2QTN = (TextView) findViewById(R.id.tv_fs2QTN);
        tv_fs3QTN = (TextView) findViewById(R.id.tv_fs3QTN);
        tv_fs4QTN = (TextView) findViewById(R.id.tv_fs4QTN);

        imgFuelLogo = (ImageView) findViewById(R.id.imgFuelLogo);
        linearHose = (LinearLayout) findViewById(R.id.linearHose);
        linear_fs_1 = (LinearLayout) findViewById(R.id.linear_fs_1);
        linear_fs_2 = (LinearLayout) findViewById(R.id.linear_fs_2);
        linear_fs_3 = (LinearLayout) findViewById(R.id.linear_fs_3);
        linear_fs_4 = (LinearLayout) findViewById(R.id.linear_fs_4);

        tv_fs1_stop.setOnClickListener(this);
        tv_fs2_stop.setOnClickListener(this);
        tv_fs3_stop.setOnClickListener(this);
        tv_fs4_stop.setOnClickListener(this);
*/
        btnGo = (Button) findViewById(R.id.btnGo);
        btnRetryWifi = (Button) findViewById(R.id.btnRetryWifi);
    }

    public void selectHoseAction(View v) {
        refreshWiFiList();
        //alertSelectHoseList(tvLatLng.getText().toString() + "\n");
    }

    public void goButtonAction(View view) {


        try {
                            cd = new ConnectionDetector(WelcomeActivity.this);
                            if (cd.isConnectingToInternet()) {
//                                Constants.AccPersonnelPIN = "";
//                                Constants.AccVehicleNumber = "";
//                                handleGetAndroidSSID("FS_UNIT51");
//                                AppConstants.LAST_CONNECTED_SSID = "FS_UNIT51";
                                Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity.class);
                                startActivity(intent);

                            } else {
                                CommonUtils.showNoInternetDialog(WelcomeActivity.this);
                            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void handleGetAndroidSSID(String selectedSSID) {

        try {


            UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);
            //----------------------------------------------------------------------------------

            selectedSSID += "#:#0#:#0";

            System.out.println("selectedSSID.." + selectedSSID);

            GetAndroidSSID getSitListAsynTask = new GetAndroidSSID(userInfoEntity.PersonEmail, selectedSSID);
            getSitListAsynTask.execute();
            getSitListAsynTask.get();

            String siteResponse = getSitListAsynTask.response;

            if (siteResponse != null && !siteResponse.isEmpty()) {

                JSONObject jsonObjectSite = new JSONObject(siteResponse);
                String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                if (ResponseMessageSite.equalsIgnoreCase("success")) {


                    String dataSite = jsonObjectSite.getString(AppConstants.RES_DATA_SSID);

                    CommonUtils.SaveDataInPref(WelcomeActivity.this, dataSite, Constants.PREF_COLUMN_SITE);

                    startWelcomeActivity();


                } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                    String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WelcomeActivity.this);
                    // set title

                    alertDialogBuilder.setTitle("Fuel Secure");
                    alertDialogBuilder
                            .setMessage(ResponseTextSite)
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
            }
            //-------------------------------------------------------------------------
        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "handleGetSitListTask", ex);
        }
    }

    private void startWelcomeActivity() {

        SharedPreferences sharedPrefODO = WelcomeActivity.this.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        IsDepartmentRequire = sharedPrefODO.getString(AppConstants.IsDepartmentRequire, "");
        IsPersonnelPINRequire = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequire, "");
        IsPersonnelPINRequireForHub = sharedPrefODO.getString(AppConstants.IsPersonnelPINRequireForHub, "");
        IsOtherRequire = sharedPrefODO.getString(AppConstants.IsOtherRequire, "");


        //Skip PinActivity and pass pin= "";
        if (Constants.CurrentSelectedHose.equals("FS1")) {
            Constants.AccPersonnelPIN_FS1 = "";
        } else if (Constants.CurrentSelectedHose.equals("FS2")) {
            Constants.AccPersonnelPIN = "";
        } else if (Constants.CurrentSelectedHose.equals("FS3")) {
            Constants.AccPersonnelPIN_FS3 = "";
        } else if (Constants.CurrentSelectedHose.equals("FS4")) {
            Constants.AccPersonnelPIN_FS4 = "";
        }

        Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity.class);
        startActivity(intent);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
/*
            case R.id.tv_fs1_stop:

                String selSSID = serverSSIDList.get(0).get("WifiSSId");
                String selMacAddress = serverSSIDList.get(0).get("MacAddress");
                String IpAddress = null;


                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL_FS_1 = "http://" + IpAddress + ":80/";
                    }
                }

                URL_GET_PULSAR_FS1 = HTTP_URL_FS_1 + "client?command=pulsar ";
                URL_SET_PULSAR_FS1 = HTTP_URL_FS_1 + "config?command=pulsar";

                URL_WIFI_FS1 = HTTP_URL_FS_1 + "config?command=wifi";
                URL_RELAY_FS1 = HTTP_URL_FS_1 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {

                    stopService(new Intent(WelcomeActivity.this, BackgroundService_AP_PIPE.class));
                    stopButtonFunctionality_FS1();
                    Constants.FS_1STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS1);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 1 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.tv_fs2_stop:

                selSSID = serverSSIDList.get(1).get("WifiSSId");
                selMacAddress = serverSSIDList.get(1).get("MacAddress");
                IpAddress = null;


                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL_FS_2 = "http://" + IpAddress + ":80/";
                    }
                }

                URL_GET_PULSAR_FS2 = HTTP_URL_FS_2 + "client?command=pulsar ";
                URL_SET_PULSAR_FS2 = HTTP_URL_FS_2 + "config?command=pulsar";
                URL_WIFI_FS2 = HTTP_URL_FS_2 + "config?command=wifi";
                URL_RELAY_FS2 = HTTP_URL_FS_2 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {
                    stopService(new Intent(WelcomeActivity.this, BackgroundService_AP.class));
                    stopButtonFunctionality_FS2();
                    Constants.FS_2STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 2 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.tv_fs3_stop:


                selSSID = serverSSIDList.get(2).get("WifiSSId");
                selMacAddress = serverSSIDList.get(2).get("MacAddress");
                IpAddress = null;


                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL_FS_3 = "http://" + IpAddress + ":80/";
                    }
                }

                URL_GET_PULSAR_FS3 = HTTP_URL_FS_3 + "client?command=pulsar ";
                URL_SET_PULSAR_FS3 = HTTP_URL_FS_3 + "config?command=pulsar";
                URL_WIFI_FS3 = HTTP_URL_FS_3 + "config?command=wifi";
                URL_RELAY_FS3 = HTTP_URL_FS_3 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {
                    stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_3.class));
                    stopButtonFunctionality_FS3();
                    Constants.FS_3STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS3);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 2 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.tv_fs4_stop:

                selSSID = serverSSIDList.get(3).get("WifiSSId");
                selMacAddress = serverSSIDList.get(3).get("MacAddress");
                IpAddress = null;


                for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                    String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                    if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                        IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                        HTTP_URL_FS_4 = "http://" + IpAddress + ":80/";
                    }
                }

                URL_GET_PULSAR_FS4 = HTTP_URL_FS_4 + "client?command=pulsar ";
                URL_SET_PULSAR_FS4 = HTTP_URL_FS_4 + "config?command=pulsar";
                URL_WIFI_FS4 = HTTP_URL_FS_4 + "config?command=wifi";
                URL_RELAY_FS4 = HTTP_URL_FS_4 + "config?command=relay";


                if (IpAddress != "" || IpAddress != null) {
                    stopService(new Intent(WelcomeActivity.this, BackgroundService_FS_UNIT_4.class));
                    stopButtonFunctionality_FS4();
                    Constants.FS_4STATUS = "FREE";
                    if (!Constants.BusyVehicleNumberList.equals(null)) {
                        Constants.BusyVehicleNumberList.remove(Constants.AccVehicleNumber_FS4);
                    }
                    // Toast.makeText(getApplicationContext(), "Fs 2 stop button pressed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please make sure your connected to FS unit", Toast.LENGTH_SHORT).show();
                }
                break;
                */
        }
    }

    public void onChangeWifiAction(View view) {
        try {

            refreshWiFiList();


        } catch (Exception ex) {
            CommonUtils.LogMessage(TAG, "onChangeWifiAction :", ex);
        }
    }

    private void refreshWiFiList() {
        new GetSSIDUsingLocation().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.CONNECTION_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                String messageData = data.getStringExtra("MESSAGE");

                if (messageData.equalsIgnoreCase("true")) {
                    Intent intent = new Intent(WelcomeActivity.this, AcceptVehicleActivity.class);
                    startActivity(intent);
                }
            }
        }

        /////////////////////////////////////////////

        switch (requestCode) {

            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("Splash", "User agreed to make required location settings changes.");

                        AppConstants.colorToast(getApplicationContext(), "Please wait...", Color.BLACK);


                        goButtonAction(null);

                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("Splash", "User chose not to make required location settings changes.");

                        AppConstants.colorToastBigFont(getApplicationContext(), "Please On GPS to connect WiFi", Color.BLUE);

                        break;
                }
                break;
        }
    }

    private boolean isNotNULL(String value) {

        boolean flag = true;
        if (value == null) {
            flag = false;
        } else if (value.trim().isEmpty()) {
            flag = false;
        } else if (value != null && value.trim().equalsIgnoreCase("null")) {
            flag = false;
        }

        return flag;
    }

    private void alertSelectHoseList(String errMsg) {


        final Dialog dialog = new Dialog(WelcomeActivity.this);
        dialog.setTitle("Fuel Secure");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_hose_list);
        //dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideAnimation;

        TextView tvNoFuelSites = (TextView) dialog.findViewById(R.id.tvNoFuelSites);
        ListView lvHoseNames = (ListView) dialog.findViewById(R.id.lvHoseNames);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        if (!errMsg.trim().isEmpty())
            tvNoFuelSites.setText(errMsg);

        if (serverSSIDList != null && serverSSIDList.size() > 0) {

            lvHoseNames.setVisibility(View.VISIBLE);
            tvNoFuelSites.setVisibility(View.GONE);

        } else {
            lvHoseNames.setVisibility(View.GONE);
            tvNoFuelSites.setVisibility(View.VISIBLE);
        }

        SimpleAdapter adapter = new SimpleAdapter(WelcomeActivity.this, serverSSIDList, R.layout.item_hose, new String[]{"item"}, new int[]{R.id.tvSingleItem});
        lvHoseNames.setAdapter(adapter);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        lvHoseNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //OnHoseSelected_OnClick(Integer.toString(position));

                new GetConnectedDevicesIP().execute();//Refreshed donnected devices list on hose selection.
                String IpAddress = "";
                SelectedItemPos = position;
                String selSSID = serverSSIDList.get(SelectedItemPos).get("WifiSSId");
                String IsBusy = serverSSIDList.get(SelectedItemPos).get("IsBusy");
                String selMacAddress = serverSSIDList.get(SelectedItemPos).get("MacAddress");
                String selSiteId = serverSSIDList.get(SelectedItemPos).get("SiteId");
                String hoseID = serverSSIDList.get(SelectedItemPos).get("HoseId");
                AppConstants.CURRENT_SELECTED_SSID = selSSID;
                AppConstants.CURRENT_SELECTED_SITEID = selSiteId;
                String IsHoseNameReplaced = serverSSIDList.get(SelectedItemPos).get("IsHoseNameReplaced");
                String ReplaceableHoseName = serverSSIDList.get(SelectedItemPos).get("ReplaceableHoseName");
                String PulserTimingAd = serverSSIDList.get(SelectedItemPos).get("PulserTimingAdjust");

                //Rename SSID while mac address updation
                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                } else {
                }

                if (selMacAddress.trim().equals("")) {  //MacAddress on server is null

                    if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                        loading = new ProgressDialog(WelcomeActivity.this);
                        loading.setCancelable(true);
                        loading.setMessage("Updating mac address please wait..");
                        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        loading.setCancelable(false);
                        loading.show();

                        //Do not enable hotspot.
                        Constants.hotspotstayOn = false;

                        //AppConstants.colorToast(WelcomeActivity.this, "Updating mac address please wait..", Color.RED);
                        wifiApManager.setWifiApEnabled(null, false);  //Hotspot disabled

                        // Toast.makeText(getApplicationContext(),"Enabled WIFI connecting to "+AppConstants.CURRENT_SELECTED_SSID,Toast.LENGTH_LONG).show();

                        WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        if (!wifiManagerMM.isWifiEnabled()) {
                            wifiManagerMM.setWifiEnabled(true);
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //new ChangeSSIDofHubStation().execute(); //Connect to selected (SSID) and Rename UserName and password of Fs unit
                                new WiFiConnectTask().execute(); //1)Connect to selected (SSID) wifi network and 2)change the ssid and password settings to connect to Hub's hotspot 3)Update MackAddress
                            }
                        }, 1000);


                    } else {
                        AppConstants.colorToastBigFont(WelcomeActivity.this, "Can't update mac address,Hose is busy please retry later.", Color.RED);
                    }

                } else {

                    try {
                        for (int i = 0; i < AppConstants.DetailsListOfConnectedDevices.size(); i++) {
                            String MA_ConnectedDevices = AppConstants.DetailsListOfConnectedDevices.get(i).get("macAddress");
                            if (selMacAddress.equalsIgnoreCase(MA_ConnectedDevices)) {
                                IpAddress = AppConstants.DetailsListOfConnectedDevices.get(i).get("ipAddress");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    if (IpAddress.equals("")) {
                        tvSSIDName.setText("Can't select this Hose not connected");
                        btnGo.setVisibility(View.GONE);

                    } else {

                        //Selected position
                        //Toast.makeText(getApplicationContext(), "FS Position" + position, Toast.LENGTH_SHORT).show();
                        AppConstants.FS_selected = String.valueOf(position);
                        if (String.valueOf(position).equalsIgnoreCase("0")) {

                            if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                                //Rename SSID from cloud
                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                } else {
                                }

                                Constants.AccPersonnelPIN = "";
                                tvSSIDName.setText(selSSID);
                                Constants.CurrentSelectedHose = "FS1";
                                btnGo.setVisibility(View.VISIBLE);
                            } else {
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);

                            }
                        } else if (String.valueOf(position).equalsIgnoreCase("1")) {
                            if (Constants.FS_2STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                                //Rename SSID from cloud
                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                } else {
                                }

                                Constants.AccPersonnelPIN = "";
                                tvSSIDName.setText(selSSID);
                                Constants.CurrentSelectedHose = "FS2";
                                btnGo.setVisibility(View.VISIBLE);
                            } else {
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);
                            }

                        } else if (String.valueOf(position).equalsIgnoreCase("2")) {


                            if (Constants.FS_3STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

                                //Rename SSID from cloud
                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                } else {
                                }

                                Constants.AccPersonnelPIN = "";
                                tvSSIDName.setText(selSSID);
                                Constants.CurrentSelectedHose = "FS3";
                                btnGo.setVisibility(View.VISIBLE);
                            } else {
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);
                            }


                        } else if (String.valueOf(position).equalsIgnoreCase("3")) {


                            if (Constants.FS_4STATUS.equalsIgnoreCase("FREE") && IsBusy.equalsIgnoreCase("N")) {
                                // linear_fs_1.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                                //Rename SSID from cloud
                                if (IsHoseNameReplaced.equalsIgnoreCase("Y")) {
                                } else {
                                }

                                Constants.AccPersonnelPIN = "";
                                tvSSIDName.setText(selSSID);
                                Constants.CurrentSelectedHose = "FS4";
                                btnGo.setVisibility(View.VISIBLE);
                            } else {
                                tvSSIDName.setText("Hose in use.\nPlease try again later");
                                btnGo.setVisibility(View.GONE);
                            }
                        } else {

                            tvSSIDName.setText("Can't select this Hose for current version");
                            btnGo.setVisibility(View.GONE);
                        }
                    }

                }
                dialog.dismiss();

            }
        });

        dialog.show();
    }

    //Connect to wifi with Password
    private void connectToWifiMarsh(String networkSSID) {


        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
            WifiConfiguration wc = new WifiConfiguration();


            wc.SSID = "\"" + networkSSID + "\"";
            wc.preSharedKey = "\"" + Constants.CurrFsPass + "\"";

            wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);


            wifiManager.setWifiEnabled(true);
            int netId = getExistingNetworkId(networkSSID);

            if (netId == -1) {
                netId = wifiManager.addNetwork(wc);
            }

            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getExistingNetworkId(String SSID) {

        SSID = "\"" + SSID + "\"";

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID != null && existingConfig.SSID.equals(SSID)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    private void AlertSettings(final Context ctx, String message) {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }

        );

        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private boolean setHotspotNamePassword(Context context) {//String newName, String newKey,
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            Method getConfigMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration wifiConfig = (WifiConfiguration) getConfigMethod.invoke(wifiManager);


            //Temp###########
           /* String CurrentHotspotName = wifiConfig.SSID;
            String CurrentHotspotPassword = wifiConfig.preSharedKey;
            if (CurrentHotspotName.equals("FS_AP_TEST") && CurrentHotspotPassword.equals("12345678")) {
                //Do nothing
            } else {

                wifiConfig.SSID = "FS_AP_TEST";
                wifiConfig.preSharedKey = "12345678";

               *//* //Disable hotspot
                wifiApManager.setWifiApEnabled(null, false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //Enable Hotsopt
                        wifiApManager.setWifiApEnabled(null, true);

                    }
                }, 100);*//*

                AppConstants.AlertDialogFinish(WelcomeActivity.this, "Configured Hotspot, Please restart the application");

            }*/

            //Temp##########

            AppConstants.HubGeneratedpassword = PasswordGeneration();
            String CurrentHotspotName = wifiConfig.SSID;
            String CurrentHotspotPassword = wifiConfig.preSharedKey;
            if (CurrentHotspotName.equals(AppConstants.HubName) && CurrentHotspotPassword.equals(AppConstants.HubGeneratedpassword)) {
                //No need to change hotspot username password

            } else {

                wifiConfig.SSID = AppConstants.HubName;
                wifiConfig.preSharedKey = AppConstants.HubGeneratedpassword;

            }

            Method setConfigMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            setConfigMethod.invoke(wifiManager, wifiConfig);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //=========================Stop button functionality for each hose==============

    @Override
    public void onBackPressed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            disconnectReader();
        }
        finish();

    }

    //==================Bluetooth Reader scan devices==================
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private synchronized void scanLeDevice(final boolean enable) {
        if (enable) {
            /* Stops scanning after a pre-defined scan period. */
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    }
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);

            invalidateOptionsMenu();
        } else if (mScanning) {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);

            invalidateOptionsMenu();
        }
    }

    /*
* Create a GATT connection with the reader. And detect the connected reader
* once service list is available.
*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean connectReader() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }

    /* Disconnects an established connection. */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void disconnectReader() {

        System.out.println("BT NFC disconnected...........");

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

    /* Get the Response string. */
    private String getResponseString(byte[] response, int errorCode) {
        if (errorCode == BluetoothReader.ERROR_SUCCESS) {
            if (response != null && response.length > 0) {
                return CommonUtils.toHexString(response);
            }
            return "";
        }
        return getErrorString(errorCode);
    }

    /* Get the Card status string. */
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

    private void PowerOnCard() {

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

    private void Authentation() {

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

    private void Startpolling() {


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

    private void TransmitApdu() {

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

    private void transmitNOSLEEPCommand() {

            /* Check for detected reader. */
        if (mBluetoothReader == null) {
            System.out.println("card_reader_not_ready");
            return;
        }

                /* Retrieve escape command from edit box. */
        byte escapeCommand[] = CommonUtils.toByteArray(DEFAULT_1255_ESCAPE_COMMAND_NO_SLEEP);

        if (escapeCommand != null && escapeCommand.length > 0) {
                    /* Clear response field for result of escape command. */


                    /* Transmit escape command. */
            if (!mBluetoothReader.transmitEscapeCommand(escapeCommand)) {
                System.out.println("card_reader_not_ready");
            } else {
                System.out.println("No Data");
            }
        } else {
            System.out.println("Character format error!");
        }

    }

    private void ReconnectFobReader() {

        t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {

                if (Constants.FS_1STATUS.equalsIgnoreCase("FREE") && Constants.FS_2STATUS.equalsIgnoreCase("FREE") && Constants.FS_3STATUS.equalsIgnoreCase("FREE") && Constants.FS_4STATUS.equalsIgnoreCase("FREE")) {

                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //Toast.makeText(getApplicationContext(),"Setting Bluetooth OFF & ON --> Reconnecting fob reader-->",Toast.LENGTH_LONG).show();
                                recreate();
                            }
                        });

                    } catch (Exception e) {

                        System.out.println(e);
                    }

                }

            }

        };
        t.schedule(tt, 3600000, 3600000);//3600000   ->5 min 300000


    }

    public class GetAndroidSSID extends AsyncTask<Void, Void, Void> {

        String Email = null;
        String latLong = null;
        String response = null;

        public GetAndroidSSID(String Email, String latLong) {
            this.Email = Email;
            this.latLong = latLong;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                //String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + Email + ":" + "AndroidSSID");
                //response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, latLong, authString);
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + Email + ":" + "AndroidSSID");
                response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, latLong, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {

                CommonUtils.LogMessage(TAG, "AuthTestAsynTask ", ex);
            }
            return null;
        }

    }

    public class GetSSIDUsingLocation extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String parm1 = AppConstants.getIMEI(WelcomeActivity.this) + ":" + userInfoEntity.PersonEmail + ":" + "Other";
                String parm2 = "Authenticate:I:" + Constants.Latitude + "," + Constants.Longitude;


                System.out.println("parm1----" + parm1);
                System.out.println("parm2----" + parm2);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                //resp = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, parm2, authString);
                //----------------------------------------------------------------------------------
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                client.setWriteTimeout(10, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(ServerHandler.TEXT, parm2);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();


                //------------------------------

            } catch (Exception e) {
                System.out.println("Ex" + e.getMessage());
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            tvLatLng.setText("Current Location :" + Constants.Latitude + "," + Constants.Longitude);

            System.out.println("GetSSIDUsingLocation...." + result);

            try {

                serverSSIDList.clear();
                //AppConstants.DetailsServerSSIDList.clear();

                String errMsg = "";

                if (result != null && !result.isEmpty()) {

                    JSONObject jsonObjectSite = new JSONObject(result);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

                        JSONArray Requests = jsonObjectSite.getJSONArray(AppConstants.RES_DATA_SSID);

                        if (Requests.length() > 0) {

                            for (int i = 0; i < Requests.length(); i++) {
                                JSONObject c = Requests.getJSONObject(i);


                                String SiteId = c.getString("SiteId");
                                String SiteNumber = c.getString("SiteNumber");
                                String SiteName = c.getString("SiteName");
                                String SiteAddress = c.getString("SiteAddress");
                                String Latitude = c.getString("Latitude");
                                String Longitude = c.getString("Longitude");
                                String HoseId = c.getString("HoseId");
                                String HoseNumber = c.getString("HoseNumber");
                                String WifiSSId = c.getString("WifiSSId");
                                String UserName = c.getString("UserName");
                                String Password = c.getString("Password");
                                String ResponceMessage = c.getString("ResponceMessage");
                                String ResponceText = c.getString("ResponceText");
                                String ReplaceableHoseName = c.getString("ReplaceableHoseName");
                                String IsHoseNameReplaced = c.getString("IsHoseNameReplaced");
                                String MacAddress = c.getString("MacAddress");
                                String IsBusy = c.getString("IsBusy");
                                String IsUpgrade = c.getString("IsUpgrade");
                                String PulserTimingAdjust = c.getString("PulserTimingAdjust");

                                //Current Fs wifi password
                                Constants.CurrFsPass = Password;

                                HashMap<String, String> map = new HashMap<>();
                                map.put("SiteId", SiteId);
                                map.put("HoseId", HoseId);
                                map.put("WifiSSId", WifiSSId);
                                map.put("ReplaceableHoseName", ReplaceableHoseName);
                                map.put("IsHoseNameReplaced", IsHoseNameReplaced);
                                map.put("item", WifiSSId);
                                map.put("MacAddress", MacAddress);
                                map.put("IsBusy", IsBusy);
                                map.put("PulserTimingAdjust", PulserTimingAdjust);

                                if (ResponceMessage.equalsIgnoreCase("success")) {
                                    if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                        serverSSIDList.add(map);
                                    }
                                } else {
                                    errMsg = ResponceText;
                                    AppConstants.AlertDialogFinish(WelcomeActivity.this, ResponceText);
                                }
                            }


                        }
                        //HoseList Alert
                        alertSelectHoseList(tvLatLng.getText().toString() + "\n" + errMsg);

                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                        String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                        AppConstants.AlertDialogBox(WelcomeActivity.this, ResponseTextSite);


                    }
                } else {
                    AppConstants.alertBigFinishActivity(WelcomeActivity.this, "Unable to connect server. Please try again later!");
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " GetSSIDUsingLocation :" + result, e);
            }

        }
    }

    public class GetSSIDUsingLocationOnResume extends AsyncTask<Void, Void, String> {


        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(WelcomeActivity.this);
            pd.setMessage("Please wait...");
            pd.setCancelable(true);
            pd.setCancelable(false);
            pd.show();

        }

        protected String doInBackground(Void... arg0) {
            String resp = "";

            try {

                UserInfoEntity userInfoEntity = CommonUtils.getCustomerDetails(WelcomeActivity.this);

                ServerHandler serverHandler = new ServerHandler();
                //----------------------------------------------------------------------------------
                String parm1 = AppConstants.getIMEI(WelcomeActivity.this) + ":" + userInfoEntity.PersonEmail + ":" + "Other";
                String parm2 = "Authenticate:I:" + Constants.Latitude + "," + Constants.Longitude;


                System.out.println("parm1----" + parm1);
                System.out.println("parm2----" + parm2);

                String authString = "Basic " + AppConstants.convertStingToBase64(parm1);

                //resp = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, parm2, authString);
                //----------------------------------------------------------------------------------
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(10, TimeUnit.SECONDS);
                client.setReadTimeout(10, TimeUnit.SECONDS);
                client.setWriteTimeout(10, TimeUnit.SECONDS);

                RequestBody body = RequestBody.create(ServerHandler.TEXT, parm2);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();


                //------------------------------

            } catch (Exception e) {
                System.out.println("Ex" + e.getMessage());
            }


            return resp;
        }


        @Override
        protected void onPostExecute(String result) {

            pd.dismiss();
            tvLatLng.setText("Current Location :" + Constants.Latitude + "," + Constants.Longitude);

            System.out.println("GetSSIDUsingLocation...." + result);

            try {

                serverSSIDList.clear();
                //AppConstants.DetailsServerSSIDList.clear();

                String errMsg = "";

                if (result != null && !result.isEmpty()) {

                    JSONObject jsonObjectSite = new JSONObject(result);
                    String ResponseMessageSite = jsonObjectSite.getString(AppConstants.RES_MESSAGE);

                    if (ResponseMessageSite.equalsIgnoreCase("success")) {

                        JSONArray Requests = jsonObjectSite.getJSONArray(AppConstants.RES_DATA_SSID);

                        if (Requests.length() > 0) {

                            for (int i = 0; i < Requests.length(); i++) {
                                JSONObject c = Requests.getJSONObject(i);


                                String SiteId = c.getString("SiteId");
                                String SiteNumber = c.getString("SiteNumber");
                                String SiteName = c.getString("SiteName");
                                String SiteAddress = c.getString("SiteAddress");
                                String Latitude = c.getString("Latitude");
                                String Longitude = c.getString("Longitude");
                                String HoseId = c.getString("HoseId");
                                String HoseNumber = c.getString("HoseNumber");
                                String WifiSSId = c.getString("WifiSSId");
                                String UserName = c.getString("UserName");
                                String Password = c.getString("Password");
                                String ResponceMessage = c.getString("ResponceMessage");
                                String ResponceText = c.getString("ResponceText");
                                String ReplaceableHoseName = c.getString("ReplaceableHoseName");
                                String IsHoseNameReplaced = c.getString("IsHoseNameReplaced");
                                String MacAddress = c.getString("MacAddress");
                                String IsBusy = c.getString("IsBusy");
                                String IsUpgrade = c.getString("IsUpgrade");
                                String PulserTimingAdjust = c.getString("PulserTimingAdjust");
                                String BluetoothCardReaderHF = c.getString("BluetoothCardReaderHF");

                                AppConstants.BT_READER_NAME = BluetoothCardReaderHF;


                                //Start Bluetooth reader scan
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                    scanLeDevice(true);
                                }

                                //Current Fs wifi password
                                Constants.CurrFsPass = Password;

                                HashMap<String, String> map = new HashMap<>();
                                map.put("SiteId", SiteId);
                                map.put("HoseId", HoseId);
                                map.put("WifiSSId", WifiSSId);
                                map.put("ReplaceableHoseName", ReplaceableHoseName);
                                map.put("IsHoseNameReplaced", IsHoseNameReplaced);
                                map.put("item", WifiSSId);
                                map.put("MacAddress", MacAddress);
                                map.put("IsBusy", IsBusy);
                                map.put("PulserTimingAdjust", PulserTimingAdjust);

                                if (ResponceMessage.equalsIgnoreCase("success")) {
                                    if (isNotNULL(SiteId) && isNotNULL(HoseId) && isNotNULL(WifiSSId)) {
                                        serverSSIDList.add(map);

                                        //#73--Only one FS unit display
                                        /*if (serverSSIDList != null && serverSSIDList.size() == 0) {

                                            tvSSIDName.setText(serverSSIDList.get(0).get("WifiSSId"));
                                            OnHoseSelected_OnClick(Integer.toString(0));

                                        }*/

                                    }
                                } else {
                                    errMsg = ResponceText;
                                    AppConstants.AlertDialogFinish(WelcomeActivity.this, ResponceText);
                                }
                            }


                        }
                        //HoseList Alert
                        //alertSelectHoseList(tvLatLng.getText().toString() + "\n" + errMsg);

                    } else if (ResponseMessageSite.equalsIgnoreCase("fail")) {
                        String ResponseTextSite = jsonObjectSite.getString(AppConstants.RES_TEXT);


                        AppConstants.AlertDialogBox(WelcomeActivity.this, ResponseTextSite);


                    }
                } else {
                    AppConstants.alertBigFinishActivity(WelcomeActivity.this, "Unable to connect server. Please try again later!");
                }


            } catch (Exception e) {

                CommonUtils.LogMessage(TAG, " GetSSIDUsingLocation :" + result, e);
            }

        }
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            ArrayList<String> connections = new ArrayList<String>();
            ArrayList<Float> Signal_Strenth = new ArrayList<Float>();

            sb = new StringBuilder();
            List<ScanResult> wifiList;
            wifiList = mainWifi.getScanResults();
            for (int i = 0; i < wifiList.size(); i++) {
                System.out.println("SSID" + wifiList.get(i).SSID);
                connections.add(wifiList.get(i).SSID);
            }


        }
    }

    public class GetConnectedDevicesIP extends AsyncTask<String, Void, String> {
        ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(WelcomeActivity.this);
            dialog.setMessage("Fetching connected device info..");
            dialog.setCancelable(false);
            dialog.show();

        }

        protected String doInBackground(String... arg0) {

            ListOfConnectedDevices.clear();

            String resp = "";

            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    BufferedReader br = null;
                    boolean isFirstLine = true;

                    try {
                        br = new BufferedReader(new FileReader("/proc/net/arp"));
                        String line;

                        while ((line = br.readLine()) != null) {
                            if (isFirstLine) {
                                isFirstLine = false;
                                continue;
                            }

                            String[] splitted = line.split(" +");

                            if (splitted != null && splitted.length >= 4) {

                                String ipAddress = splitted[0];
                                String macAddress = splitted[3];
                                System.out.println("IPAddress" + ipAddress);
                                boolean isReachable = InetAddress.getByName(
                                        splitted[0]).isReachable(500);  // this is network call so we cant do that on UI thread, so i take background thread.
                                if (isReachable) {
                                    Log.d("Device Information", ipAddress + " : "
                                            + macAddress);
                                }

                                if (ipAddress != null || macAddress != null) {

                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("ipAddress", ipAddress);
                                    map.put("macAddress", macAddress);

                                    ListOfConnectedDevices.add(map);

                                }
                                AppConstants.DetailsListOfConnectedDevices = ListOfConnectedDevices;

                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();


            return resp;


        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            String strJson = result;


            dialog.dismiss();

        }

    }

    //-------------------Bluetooth Reader------------

    public class CommandsGET_INFO extends AsyncTask<String, Void, String> {

        public String resp = "";


        protected String doInBackground(String... param) {


            try {

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url(param[0])
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
                Constants.hotspotstayOn = true;
                loading.dismiss();
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {

            System.out.println(" resp......." + result);
            System.out.println("2:" + Calendar.getInstance().getTime());

        }
    }

    public class CommandsPOST_ChangeHotspotSettings extends AsyncTask<String, Void, String> {

        public String resp = "";

        protected String doInBackground(String... param) {

            System.out.println("url" + HTTP_URL);
            try {


                MediaType JSON = MediaType.parse("application/json");

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, param[1]);

                Request request = new Request.Builder()
                        .url(param[0])
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                loading.dismiss();
                Constants.hotspotstayOn = true;
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {


            try {

                consoleString += "OUTPUT- " + result + "\n";
                //loading.dismiss();


                System.out.println(result);

            } catch (Exception e) {
                loading.dismiss();
                Constants.hotspotstayOn = true;
                System.out.println(e);
            }

        }
    }

    private class WiFiConnectTask extends AsyncTask<String, Void, String> {
        // Do the long-running work in here
        protected String doInBackground(String... asd) {

/*
            //Forget Netwotk
            try {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for (WifiConfiguration i : list) {

                    if (i.SSID.equalsIgnoreCase("\"" + AppConstants.CURRENT_SELECTED_SSID + "\"")) {
                        wifiManager.disableNetwork(i.networkId);
                        break;
                    }
                    // wifiManager.removeNetwork(i.networkId);
                    // wifiManager.saveConfiguration();
                }
            }catch (NullPointerException e){System.out.println(e);}*/
            connectToWifiMarsh(AppConstants.CURRENT_SELECTED_SSID);
            // connectCustom(AppConstants.CURRENT_SELECTED_SSID);

            return "";
        }


        @Override
        protected void onPostExecute(String s) {


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    WifiManager wifiManager = (WifiManager) WelcomeActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                    int NetID = wifiInfo.getNetworkId();
                    String ssid = wifiInfo.getSSID();
                    if (!(NetID == -1) && ssid.contains("\"" + AppConstants.CURRENT_SELECTED_SSID + "\"")) {

                        AppConstants.WriteinFile("WelcomeActivity~~~~~~~~~" + "WIFI CONNECTED " + AppConstants.CURRENT_SELECTED_SSID);
                        AppConstants.colorToastBigFont(WelcomeActivity.this, "CONNECTED TO: " + AppConstants.CURRENT_SELECTED_SSID, Color.BLUE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                AppConstants.colorToastBigFont(WelcomeActivity.this, "  Changing ssid and password settings  ", Color.BLUE);
                                HTTP_URL = "http://192.168.4.1/";
                                URL_UPDATE_FS_INFO = HTTP_URL + "config?command=wifi";
                                String jsonChangeUsernamePass = "{\"Request\":{\"Station\":{\"Connect_Station\":{\"ssid\":\"" + AppConstants.HubName + "\",\"password\":\"" + AppConstants.HubGeneratedpassword + "\"}}}}";

                                new CommandsPOST_ChangeHotspotSettings().execute(URL_UPDATE_FS_INFO, jsonChangeUsernamePass);

                                btnRetryWifi.setVisibility(View.GONE);

                            }
                        }, 1000);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                HTTP_URL = "http://192.168.4.1:80/";
                                URL_INFO = HTTP_URL + "client?command=info";
                                try {
                                    String result = new CommandsGET_INFO().execute(URL_INFO).get();

                                    String mac_address = "";
                                    if (result.contains("Version")) {
                                        JSONObject jsonObject = new JSONObject(result);
                                        JSONObject joPulsarStat = jsonObject.getJSONObject("Version");
                                        String sdk_version = joPulsarStat.getString("sdk_version");
                                        String iot_version = joPulsarStat.getString("iot_version");
                                        mac_address = joPulsarStat.getString("mac_address");//station_mac_address

                                        if (mac_address.equals("")) {
                                            loading.dismiss();
                                            Constants.hotspotstayOn = true;
                                            AppConstants.colorToastBigFont(WelcomeActivity.this, "Could not get mac address", Color.RED);
                                            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                            if (wifiManagerMM.isWifiEnabled()) {
                                                wifiManagerMM.setWifiEnabled(false);
                                            }
                                            //wifiApManager.setWifiApEnabled(null, true);//enable hotspot

                                        } else {
                                            //Rename FluidSecure Unite (pipe)
                                             //RenameLink();

                                            AppConstants.UPDATE_MACADDRESS = mac_address;
                                            AppConstants.colorToastBigFont(WelcomeActivity.this, "Mac address " + mac_address, Color.BLUE);
                                            WifiManager wifiManagerMM = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                            if (wifiManagerMM.isWifiEnabled()) {
                                                wifiManagerMM.setWifiEnabled(false);
                                            }

                                            // wifiApManager.setWifiApEnabled(null, true);//enable hotspot
                                            // -----------------------------

                                            try {

                                                UpdateMacAddressClass authEntityClass1 = new UpdateMacAddressClass();
                                                authEntityClass1.SiteId = Integer.parseInt(AppConstants.CURRENT_SELECTED_SITEID);
                                                authEntityClass1.MACAddress = AppConstants.UPDATE_MACADDRESS;
                                                authEntityClass1.RequestFrom = "AP";
                                                authEntityClass1.HubName = AppConstants.HubName;

                                                //------
                                                Gson gson = new Gson();
                                                final String jsonData = gson.toJson(authEntityClass1);

                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {


                                                        cd = new ConnectionDetector(WelcomeActivity.this);
                                                        if (cd.isConnectingToInternet()) {

                                                            new UpdateMacAsynTask().execute(jsonData);//,AppConstants.getIMEI(WelcomeActivity.this)
                                                            //Update SSID change status to server(SSID to server)
                                                            //UpdateSSIDStatusToServer();

                                                        } else {
                                                            AppConstants.colorToast(WelcomeActivity.this, "Please check Internet Connection and retry.", Color.RED);
                                                            // loading.dismiss();
                                                            // new UpdateMacAsynTask().execute(jsonData);
                                                        }

                                                    }
                                                }, 8000);

                                            } catch (Exception e) {
                                                loading.dismiss();
                                                Constants.hotspotstayOn = true;
                                                System.out.println(e);
                                            }

                                        }
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 2000);

                       /* //Check For Rename
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                RenameLink();
                            }
                        }, 3000);
*/


                    } else {
                        //String autoNetworkSwitch = getExternalString(DisplayMeterActivity.this, "com.android.settings","wifi_watchdog_connectivity_check", "Unknown");
                        AppConstants.WriteinFile("WelcomeActivity~~~~~~~~~" + "WIFI NOT CONNECTED " + AppConstants.CURRENT_SELECTED_SSID);
                        RetryOneAtemptConnectToSelectedSSSID += 1;
                        if (RetryOneAtemptConnectToSelectedSSSID < 4) {
                            //gooooo
                            AppConstants.colorToastBigFont(getApplicationContext(), "Attempt:"+RetryOneAtemptConnectToSelectedSSSID+"\nReconnecting to " + AppConstants.CURRENT_SELECTED_SSID, Color.RED);
                            new WiFiConnectTask().execute();
                        } else {

                            btnRetryWifi.setVisibility(View.VISIBLE);

                            if (!isTCancelled)
                                AlertSettings(WelcomeActivity.this, "Unable to connect " + AppConstants.CURRENT_SELECTED_SSID + "!\n\nPlease connect to " + AppConstants.CURRENT_SELECTED_SSID + " manually using the 'WIFI settings' screen.\nThen hit back and click on the 'START' button to continue.");
                        }

                    }
                }
            }, 12000);

        }
    }

    public class UpdateMacAsynTask extends AsyncTask<String, Void, String> {


        public String response = null;

        @Override
        protected String doInBackground(String... param) {

            try {
                ServerHandler serverHandler = new ServerHandler();


                String jsonData = param[0];
                String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;

                //----------------------------------------------------------------------------------
                String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "UpdateMACAddress");
                response = serverHandler.PostTextData(WelcomeActivity.this, AppConstants.webURL, jsonData, authString);
                //----------------------------------------------------------------------------------

            } catch (Exception ex) {
                loading.dismiss();
                Constants.hotspotstayOn = true;
                CommonUtils.LogMessage("", "UpdateMACAddress ", ex);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String serverRes) {

            try {
                if (serverRes != null) {


                    JSONObject jsonObject1 = new JSONObject(serverRes);

                    String ResponceMessage = jsonObject1.getString("ResponceMessage");


                    if (ResponceMessage.equalsIgnoreCase("success")) {


                        loading.dismiss();
                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Mac Address Updated ", Color.parseColor("#4CAF50"));
                        wifiApManager.setWifiApEnabled(null, true);


                    } else if (ResponceMessage.equalsIgnoreCase("fail")) {
                        loading.dismiss();
                        Constants.hotspotstayOn = true;
                        AppConstants.colorToastBigFont(WelcomeActivity.this, " Could not Updated mac address ", Color.RED);
                        wifiApManager.setWifiApEnabled(null, true);
                    }

                } else {
                    CommonUtils.showNoInternetDialog(WelcomeActivity.this);
                }
            } catch (Exception e) {

            }
        }
    }

    public class ChangeBusyStatus extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... param) {
            String resp = "";

            String userEmail = CommonUtils.getCustomerDetails(WelcomeActivity.this).PersonEmail;

            String authString = "Basic " + AppConstants.convertStingToBase64(AppConstants.getIMEI(WelcomeActivity.this) + ":" + userEmail + ":" + "UpgradeIsBusyStatus");

            RenameHose rhose = new RenameHose();
            rhose.SiteId = AppConstants.CURRENT_SELECTED_SITEID;


            Gson gson = new Gson();
            String jsonData = gson.toJson(rhose);


            try {
                OkHttpClient client = new OkHttpClient();
                MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");

                RequestBody body = RequestBody.create(TEXT, jsonData);
                Request request = new Request.Builder()
                        .url(AppConstants.webURL)
                        .post(body)
                        .addHeader("Authorization", authString)
                        .build();

                Response response = client.newCall(request).execute();
                resp = response.body().string();

            } catch (Exception e) {
                Log.d("Ex", e.getMessage());
            }


            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            try {

                // pd.dismiss();
                System.out.println("eeee" + result);

            } catch (Exception e) {
                System.out.println("eeee" + e);
            }
        }
    }


    //========================ends=========================================


}