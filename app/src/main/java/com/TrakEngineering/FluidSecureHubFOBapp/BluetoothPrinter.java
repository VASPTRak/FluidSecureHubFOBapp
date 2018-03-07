package com.TrakEngineering.FluidSecureHubFOBapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static com.TrakEngineering.FluidSecureHubFOBapp.BluetoothReaderReciver.ctx;

/**
 * Created by User on 12/20/2017.
 */

public class BluetoothPrinter {


    TextView myLabel;

    // will enable user to enter any text to be printed
    EditText myTextbox;

    // android built in classes for bluetooth operations
    static BluetoothAdapter mBluetoothAdapter;
    static BluetoothSocket mmSocket;
    static BluetoothDevice mmDevice;

    // needed for communication to bluetooth device / network
    static OutputStream mmOutputStream;
    static InputStream mmInputStream;
    static Thread workerThread;

    static byte[] readBuffer;
    static int readBufferPosition;
    static volatile boolean stopWorker;


    public static boolean findBT() {


        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                //myLabel.setText("No bluetooth adapter available");
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    String MacAddr = AppConstants.PrinterMacAddress;
                    if (MacAddr.equalsIgnoreCase(""))
                    {

                        if (device.getName().equals(AppConstants.BLUETOOTH_PRINTER_NAME)) {//Sony= "C4:3A:BE:79:B1:C5" //HHW-UART-S10
                            mmDevice = device;
                            break;
                        }


                    }else{

                        if (device.getName().equals(AppConstants.BLUETOOTH_PRINTER_NAME) & device.getAddress().equalsIgnoreCase(MacAddr)) {//Sony= "C4:3A:BE:79:B1:C5" //HHW-UART-S10
                            mmDevice = device;
                            break;
                        }else{
                            Toast.makeText(ctx,"printer mac address blank",Toast.LENGTH_LONG).show();
                        }


                    }

                }
            }

            //myLabel.setText("Bluetooth device found.");

        }catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }

    public static void openBT() throws IOException {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            //myLabel.setText("Bluetooth Opened");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                               // myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   public static void sendData(String printReceipt) throws IOException {
        try {

            // the text typed by the user
            String msg = printReceipt;//myTextbox.getText().toString();
            msg += "\n";

            mmOutputStream.write(msg.getBytes());

            // tell the user data were sent
            //myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void closeBT() throws IOException {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            myLabel.setText("Bluetooth Closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
