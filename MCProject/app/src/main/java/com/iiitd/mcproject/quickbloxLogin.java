package com.iiitd.mcproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Shubham on 01 Nov 14.
 */
public class quickbloxLogin extends AsyncTask<Void, Void, String>
{
    private String userLogin;
    private String userEmail;
    private String userFBid;
    private String userFullName;
    private String userPassword;
    private String usertokenQB;

    private StringBuilder sb = new StringBuilder();

    private String urlQB = "http://api.quickblox.com/users.json";

    private JSONObject loginObj = new JSONObject();
    private JSONObject userObj = new JSONObject();

    private ProgressDialog pDialog;
    private Context context;

    public quickbloxLogin (String a, String b, String c, String d, String e, String f, Context cnt)
    {
        this.userLogin = a;
        this.userPassword = b;
        this.userEmail = c;
        this.userFBid = d;
        this.userFullName = e;
        this.usertokenQB = f;
        this.context = cnt;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Signing user up...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(Void... params)
    {
        URL object = null;
        try {
            object = new URL(this.urlQB);
            HttpURLConnection con = (HttpURLConnection) object.openConnection();

            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("QuickBlox-REST-API-Version", "0.1.0");
            con.setRequestProperty("QB-Token",usertokenQB);

            Log.d("token",usertokenQB);

            con.setRequestMethod("POST");

            userObj.put("login",userLogin);
            userObj.put("password",userPassword);
            userObj.put("email",userEmail);
            userObj.put("facebook_id",userFBid);
            userObj.put("full_name",userFullName);

            loginObj.put("user", userObj);

            Log.d("debug", loginObj.toString());

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());

            wr.write(loginObj.toString());

            wr.flush();

            int HttpResult = con.getResponseCode();

            Log.d("response code"," "+HttpResult);

            if (HttpResult == 201)
            {

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

                String line = null;

                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                br.close();

                Log.d("debug", "" + sb.toString());

            } else {
                System.out.println(con.getResponseMessage());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Override
    protected void onPostExecute(String str)
    {
        pDialog.dismiss();
        if(str != null)
            Log.d("system response",str);
        else
            Log.d("system response","is null");
    }
}
