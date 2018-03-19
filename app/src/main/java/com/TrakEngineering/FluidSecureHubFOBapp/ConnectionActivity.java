package com.TrakEngineering.FluidSecureHubFOBapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectionActivity extends AppCompatActivity {

    private static final int SERVER_PORT = 2901;
    private static final String SERVER_IP = "192.168.4.1";
    private TextView tvClientMsg;

    private EditText editText;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvClientMsg = (TextView) findViewById(R.id.textViewClientMessage);
        editText = (EditText) findViewById(R.id.editText);


    }


    public void sendMessage(View view) {
        try {

            String comment = editText.getText().toString().trim();

            if (comment != null) {
                new ServerConnectionTask(SERVER_IP, SERVER_PORT, tvClientMsg, comment).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            } else if (comment.contains("stop fueling")) {
                if (socket == null) {

                    socket.close();

                    new ServerConnectionTask(SERVER_IP, SERVER_PORT, tvClientMsg, comment).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
                }
            }


        } catch (Exception ex) {
            Log.e("", ex.getMessage());

        }

    }

    public void connectServer(View view) {
        try {


            if (socket == null || socket.isClosed()) {
                try {
                    new ServerConnectionTask(SERVER_IP, SERVER_PORT, tvClientMsg, null).execute();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }


        } catch (Exception ex) {
            Log.e("", ex.getMessage());

        }

    }


    public class ServerConnectionTask extends AsyncTask<Void, Void, String> {

        String TAG = "ServerConneTask";
        String dstAddress;
        int dstPort;
        String response = "";
        TextView textResponse;
        String command;

        ServerConnectionTask(String addr, int port, TextView textResponse, String Command) {
            dstAddress = addr;
            dstPort = port;
            this.textResponse = textResponse;
            this.command = Command;
        }

        @Override
        protected String doInBackground(Void... arg0) {

            Log.d(TAG, "Entry in doinbackroung");

            try {


                if (socket == null || socket.isClosed()) {
                    try {

                        Log.d(TAG, "Before Socket open");
                        socket = new Socket(dstAddress, dstPort);

                        Log.d(TAG, "After Socket open");

                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }


                if (command != null) {

                    Log.d(TAG, "Before sent " + command);
                    DataOutputStream DataOut = new DataOutputStream(socket.getOutputStream());
                    DataOut.write(command.getBytes("US-ASCII"));
                    DataOut.flush();

                    Log.d(TAG, "After sent command " + command);

                }


                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);

                InputStream inputStream = socket.getInputStream();
                Log.d(TAG, "After get Input Stream ");
                byte[] buffer = new byte[102400];


                int bytesRead;

                Log.d(TAG, "Before while loop ");

                int count = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    Log.d(TAG, "IN while loop ");

                    byteArrayOutputStream.write(buffer, 0, bytesRead);

                    Log.d(TAG, "bytesRead " + bytesRead);


                    if (command == null || !command.contains("begin fueling")) {


                        response += byteArrayOutputStream.toString("UTF-8");
                        Log.d(TAG, "response : " + response);

                        Log.d(TAG, "In cmd if : ");
                        break;
                    } else {

                        Log.d(TAG, "In CMD else ");
                        final int reading = count++;
                        response = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
                        Log.d(TAG, "response : " + response + "reading " + reading);


                        if (!response.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Log.d(TAG, "In UI thread ");
                                    textResponse.setText("" + reading + " : " + response);

                                }
                            });
                        }
                        if (reading == 130) {
                            break;
                        }

                    }
                }


            } catch (UnknownHostException e) {

                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();

                Log.d(TAG, "response : " + response);
            } catch (IOException e) {

                e.printStackTrace();
                response = "IOException: " + e.toString();

                Log.d(TAG, "response : " + response);
            } finally {

                Log.d(TAG, "In finally : ");

               /* if (socket != null) {
                    try {
                        socket.close();
                        Log.d(TAG, "Socket is closed : ");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error while socket closing : ");
                    }
                }*/

            }
            return response;
        }

        @Override
        protected void onPostExecute(final String res) {

            Log.d(TAG, "Entering onPostExecute");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String result = res;
                    Log.d(TAG, "Entering Ui Thread");

                    Log.d(TAG, "res in Thread before if" + res);
                    if (!result.equalsIgnoreCase("connect")) {

                        result = result.replace("connect", "");
                    }
                    Log.d(TAG, "res in Thread After if" + res);
                    textResponse.setText(result);

                }
            });

            super.onPostExecute(res);
        }

    }


}
