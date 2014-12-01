package com.iiitd.mcproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by Shubham on 20 Nov 14.
 */
public class userLoginRequest extends AsyncTask<Void, Void, String> {

    private final String UrlRails = "https://tranquil-stream-2635.herokuapp.com/login";

    private StringBuilder sb = new StringBuilder();

    private String userEmail;
    private String userLogin;
    private String userPassword;
    private Context context;

    private String userQBId;
    private String userRailsID;
    private String userRailsToken;

    private ProgressDialog pDialog;

    public userLoginRequest(String a, String b, Context cnt)
    {
        this.userEmail = a;
        this.userPassword=b;
        this.context = cnt;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Logging in...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(Void... params)
    {
        InputStream inputStream = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(UrlRails);

            String json = "";

            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("email", userEmail);
                jsonObject.put("password", userPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            json = jsonObject.toString();
            Log.d("json", json);

            StringEntity se = new StringEntity(json);
            httpPost.setHeader("Accept","application/json");
            httpPost.setHeader("Content-type","application/json");
            httpPost.setEntity(se);

            org.apache.http.HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
            StatusLine sl = httpResponse.getStatusLine();

//            Log.v("debug", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase());

            try {
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    sb.append((char) ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    protected void onPostExecute(String str)
    {
        pDialog.dismiss();
        if(str != null)
        {
            Log.d("system response in login",str);
            getUserDetails(str);
            RailsServerSignUp r1 = new RailsServerSignUp(context);
            r1.userChat_auth();
        }
        else
            Log.d("system response","is null");
    }

    private void getUserDetails(String string)
    {
        string = string.substring(1,string.length()-1);
        String[] res = string.split(",");
        for (String abc : res) {
            String[] pair = abc.split(":");
            if(pair[0].equals("\"chat_id\""))
                userQBId = pair[1];
            if (pair[0].equals("\"id\""))
                userRailsID = pair[1];
            if (pair[0].equals("\"name\""))
            {
                userLogin = pair[1];
                userLogin = userLogin.substring(1,userLogin.length()-1);
            }
            if(pair[0].equals("\"token\""))
            {
                userRailsToken = pair[1];
                userRailsToken = userRailsToken.substring(1,userRailsToken.length()-1);
            }
        }
        Log.d("chat_id",userQBId);
        Log.d("railsID",userRailsID);
        Log.d("railsToken",userRailsToken);
        SharedPreferences sharedPref;
        sharedPref = context.getSharedPreferences(Common.PREF, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userLogin",userLogin);
        editor.putString("userQBId",userQBId);
        editor.putString("userRailsID",userRailsID);
        editor.putString("userRailsToken",userRailsToken);
        editor.commit();

        SharedPreferences sp = context.getSharedPreferences(Common.PREF,Context.MODE_PRIVATE);
        String checkSP1 = sp.getString("userLogin","null");
        String checkSP2 = sp.getString("userPassword","null");
        String checkSP3 = sp.getString("userEmail","null");
        String checkSP4 = sp.getString("userQBId","null");
        String checkSP5 = sp.getString("userRailsID","null");
        String checkSP6 = sp.getString("userRailsToken","null");
        Log.d("checkSP1",checkSP1);
        Log.d("checkSP2",checkSP2);
        Log.d("checkSP3",checkSP3);
        Log.d("checkSP4",checkSP4);
        Log.d("checkSP5",checkSP5);
        Log.d("checkSP6",checkSP6);
    }

}
