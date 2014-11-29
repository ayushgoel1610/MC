package com.iiitd.mcproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.iiitd.mcproject.Chat.ui.activities.NewDialogActivity;
import com.iiitd.mcproject.Chat.ui.activities.SplashActivity;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.users.model.QBUser;
import com.iiitd.mcproject.ApplicationSingleton;
import com.iiitd.mcproject.R;

import org.jivesoftware.smack.SmackException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Shubham on 01 Nov 14.
 */
public class quickbloxLogin extends AsyncTask<Void, Void, String>
{
    private String userLogin;
    private String userEmail;
    private String userPassword;
    private String usertokenQB;

    private static final String APP_ID = "15476";
    private static final String AUTH_KEY = "GeO3pbwR999HM9w";
    private static final String AUTH_SECRET = "h9LvUs4uLShEG7S";

    //private static final String USER_LOGIN = "ayushgoel1610_6682746_32656";
    //private static final String USER_PASSWORD = "password";

//    private static final String USER_LOGIN = "testuser";
//    private static final String USER_PASSWORD = "password";

    private static String USER_LOGIN ;
    private static String USER_PASSWORD;
    private static String USER_QB_ID;

    static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    private QBChatService chatService;

    private StringBuilder sb = new StringBuilder();

    private String urlQB = "http://api.quickblox.com/users.json";

    private JSONObject loginObj = new JSONObject();
    private JSONObject userObj = new JSONObject();

    private ProgressDialog pDialog;
    private Context context;

    public quickbloxLogin(Context cnt)
    {
        this.context = cnt;
    }

    public quickbloxLogin (String a, String b, String c, String d, Context cnt)
    {
        this.userLogin = a;
        this.userPassword = b;
        this.userEmail = c;
        this.usertokenQB = d;
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
//        userChat_auth();
        pDialog.dismiss();
        if(str != null)
        {
            Log.d("system response",str);
            getQBId(str);
            if(USER_QB_ID!=null)
            {
                Log.d("QB ID:",USER_QB_ID);
                SharedPreferences sharedPref;
                sharedPref = context.getSharedPreferences(Common.PREF, context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userQBId",USER_QB_ID);
                editor.commit();
            }
            else
                Log.d("QB ID:","not found");
            RailsServerSignUp r1 = new RailsServerSignUp(userLogin,userEmail,userPassword,usertokenQB,USER_QB_ID,context);
            r1.execute();
        }
        else
            Log.d("system response","is null");

    }

    private void getQBId(String inputStr)
    {
        try {
            inputStr = inputStr.substring(9, inputStr.length() - 2);
            String[] res = inputStr.split(",");
            for (String abc : res) {
                Log.d("qb strings", abc);
                String[] pair = abc.split(":");
                if (pair[0].equals("\"id\""))
                    USER_QB_ID = pair[1];
                break;
            }
        }
        catch (StringIndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }
}
