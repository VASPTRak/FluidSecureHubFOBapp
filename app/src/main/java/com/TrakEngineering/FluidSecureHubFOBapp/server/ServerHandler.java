package com.TrakEngineering.FluidSecureHubFOBapp.server;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.TrakEngineering.FluidSecureHubFOBapp.AppConstants;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ServerHandler {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    public static final MediaType JSON = MediaType.parse("application/json;charset=UTF-8");//("application/x-www-form-urlencoded");

    public static final MediaType TEXT = MediaType.parse("application/text;charset=UTF-8");//("application/x-www-form-urlencoded");
    public OkHttpClient client = new OkHttpClient();
    private SharedPreferences sh_Pref;
    private String TAG = "ServerHandler : ";

    // constructor
    public ServerHandler() {

    }

    public String PostFromService(Context activity, String serverUrl, String JsonData, String authTokan) throws IOException, Exception {



        RequestBody body = RequestBody.create(JSON, JsonData);
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(body)
                .addHeader("Authorization", authTokan)
                .build();

        Response response;
        response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return response.body().string();

    }

    // This post method useful for only post to getting pump numbers and
    public String PostTextData(Context activity, String serverUrl, String JsonData, String authTokan) throws IOException, Exception {



        RequestBody body = RequestBody.create(TEXT, JsonData);
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(body)
                .addHeader("Authorization", authTokan)
                .build();

        Response response;
        response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return response.body().string();

    }

    public Response Post(Context activity, String serverUrl, String JsonData, String authTokan) throws IOException, Exception {

        RequestBody body = RequestBody.create(JSON, JsonData);
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(body)
                .addHeader("Authorization", authTokan)
                .build();

        Response response;
        response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return response;//response.body().string();

    }
    public String uploadFile(String path, String authBas64String, String SerialNo, String isSign, int index) throws IOException {
        String LocalPath = path;
        File f1 = new File(LocalPath);
        String FileName = f1.getName();
        String sContent = "form-data; name=\"file\";filename=\"" + FileName + "\"";
        MediaType MEDIA_TYPE_FILE = MediaType.parse("application/octet-stream");
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addPart(Headers.of("Content-Disposition", sContent), RequestBody.create(MEDIA_TYPE_FILE, new File(path)))
                .build();
        Request request = new Request.Builder()
                .url(AppConstants.webURL)
                .post(requestBody)
                .addHeader("Authorization", authBas64String)
                .addHeader("Serial", SerialNo)
                .addHeader("IsSign", isSign)
                .addHeader("Index", Integer.toString(index))
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
        return response.body().string();
    }

    public String put(Activity actvity, String url, String marchandId) throws IOException, Exception {
        String xtoken = getXtoken(actvity);
        RequestBody body = RequestBody.create(JSON, marchandId);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("x-auth-token", xtoken)
                .build();
        Response response;
        response = client.newCall(request).execute();
        return response.body().string();

    }

    public String Delete(Activity actvity, String url, String marchandId) throws IOException, Exception {

        String xtoken = getXtoken(actvity);
        url = url + marchandId;
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("x-auth-token", xtoken)
                .build();
        Response response;
        response = client.newCall(request).execute();
        return response.body().string();

    }

    public Response get(Activity activity, String url, String authTokan) throws IOException, Exception {


        Request request = new Request.Builder()
                .url(url)
                .get().addHeader("Authorization", authTokan)
                .build();
        Response response;
        response = client.newCall(request).execute();
        return response;

    }

    private String getXtoken(Activity actvity) {
        String xtoken = null;
        try {
            //sh_Pref = actvity.getSharedPreferences(Constants.prefLoginName, actvity.MODE_PRIVATE);
            //xtoken=sh_Pref.getString(Constants.accessToken, "invalid");

        } catch (Exception e) {

            Log.e(TAG, e.getMessage());
        }
        return xtoken;
    }

}
