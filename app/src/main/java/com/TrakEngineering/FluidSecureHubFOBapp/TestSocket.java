package com.TrakEngineering.FluidSecureHubFOBapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TestSocket extends AppCompatActivity {

    private static final int SERVER_PORT = 2901;
    private static final String SERVER_IP = "192.168.4.1";
    private Button btnSocketOn;
    private Button btnSocketOff;
    private Button btnturnonrelay;
    private Button btnbeginfueling;
    private Button btnquantity;
    private Button btnturnoffrelay;
    private Button btnrecordcheck;
    private Button btnConnect;
    private TextView tvResult;
    private String TURNONRELAY = "turn on relay";
    private String BEGINFUEL = "begin fueling";
    private String PULSORCOUNT = "quantity";
    private String TURNOFFRELAY = "turn off relay";
    private String GETHISTORY = "record check";


    private String TAG = "TestSocket...";

    private Socket socketFS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_socket);
        btnSocketOn = (Button) findViewById(R.id.btnSocketOn);
        btnSocketOff = (Button) findViewById(R.id.btnSocketOff);
        btnturnonrelay = (Button) findViewById(R.id.btnturnonrelay);
        btnbeginfueling = (Button) findViewById(R.id.btnbeginfueling);
        btnquantity = (Button) findViewById(R.id.btnquantity);
        btnturnoffrelay = (Button) findViewById(R.id.btnturnoffrelay);
        btnrecordcheck = (Button) findViewById(R.id.btnrecordcheck);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        tvResult = (TextView) findViewById(R.id.tvResult);


        btnSocketOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (socketFS != null && socketFS.isConnected()) {

                    tvResult.setText("Socket Already opened");

                } else {

                    try {
                        socketFS = new Socket();

                        tvResult.setText("Socket Open");

                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            }
        });

        btnSocketOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    socketFS.close();
                    tvResult.setText("Socket Closed");

                } catch (Exception e) {
                    System.out.println(e);
                }

            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new TCPConnectSocket().execute();
            }
        });

        btnturnonrelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TCPClientTask().execute(TURNONRELAY);

            }
        });

        btnbeginfueling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TCPClientTask().execute(BEGINFUEL);
            }
        });

        btnquantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TCPClientTask().execute(PULSORCOUNT);
            }
        });

        btnturnoffrelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TCPClientTask().execute(TURNOFFRELAY);
            }
        });

        btnrecordcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TCPClientTask().execute(GETHISTORY);
            }
        });

    }


    public class TCPClientTask extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected String doInBackground(String... command) {

            InputStream inputStream;
            try {

                String strcmd = command[0];
                System.out.println("strcmd......" + strcmd);


                if (!socketFS.isConnected()) {
                    socketFS.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 5000);//
                }

                DataOutputStream DataOut = new DataOutputStream(socketFS.getOutputStream());
                DataOut.writeBytes(strcmd);
                DataOut.flush();


                inputStream = socketFS.getInputStream();


                Log.d(TAG, "TCPClientTask Input Stream ");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];

                Log.d(TAG, "TCPClientTask Before while loop ");

                try {

                    response = "";

                    int bytesRead = 0;

                    if ((bytesRead = inputStream.read(buffer)) != -1) {

                        byteArrayOutputStream.write(buffer, 0, bytesRead);

                        response = byteArrayOutputStream.toString("UTF-8");

                        Log.d(TAG, "response1 : " + response);
                    }


                } catch (Exception e) {
                    Log.d(TAG, "e : " + e);
                }

                Log.d(TAG, "output : " + response);

            } catch (Exception e) {
                System.out.println("TCPClientTask......" + e);
                response = e.getMessage();
            } finally {
                Log.d(TAG, "finally output : " + response);
                try {
                    //socketFS.close();
                } catch (Exception e) {

                }


            }
            return response;
        }

        @Override
        protected void onPostExecute(String res) {

            tvResult.setText(res);

        }

    }


    public class TCPConnectSocket extends AsyncTask<String, Void, String> {

        String response = "";

        @Override
        protected String doInBackground(String... command) {

            InputStream inputStream;
            try {


                if (!socketFS.isConnected()) {
                    socketFS.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 5000);//
                }

                inputStream = socketFS.getInputStream();


                Log.d(TAG, "TCPConnectSocket Input Stream ");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];


                Log.d(TAG, "TCPConnectSocket Before while loop ");

                try {

                    response = "";

                    int bytesRead = 0;

                    if ((bytesRead = inputStream.read(buffer)) != -1) {

                        byteArrayOutputStream.write(buffer, 0, bytesRead);

                        response = byteArrayOutputStream.toString("UTF-8");

                        Log.d(TAG, "TCPConnectSocket : " + response);
                    }


                } catch (Exception e) {
                    Log.d(TAG, "e : " + e);
                }

                Log.d(TAG, "output : " + response);

            } catch (Exception e) {
                System.out.println("TCPConnectSocket......" + e);
                response = e.getMessage();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String res) {

            tvResult.setText(res);

        }

    }

}
