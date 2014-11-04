package com.iiitd.mcproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
    private String userFBid;
    private String userFullName;
    private String userPassword;
    private String usertokenQB;

    private static final String APP_ID = "15476";
    private static final String AUTH_KEY = "GeO3pbwR999HM9w";
    private static final String AUTH_SECRET = "h9LvUs4uLShEG7S";
    //
    private static final String USER_LOGIN = "test";
    private static final String USER_PASSWORD = "password";

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
        userChat_auth();
        pDialog.dismiss();
        if(str != null)
            Log.d("system response",str);
        else
            Log.d("system response","is null");
    }

    public void userChat_auth(){
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
        }
        chatService = QBChatService.getInstance();


        // create QB user
        //
        final QBUser user = new QBUser();
        user.setLogin(USER_LOGIN);
        user.setPassword(USER_PASSWORD);
        QBAuth.createSession(user, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle args) {

                // save current user
                //
                user.setId(session.getUserId());
                //((ApplicationSingleton)getApplication()).setCurrentUser(user);
                Log.d("debug","on success");

                // login to Chat
                //
                loginToChat(user);
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("create session errors: " + errors).create().show();
            }
        });

    }

    private void loginToChat(final QBUser user){

        chatService.login(user, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                Log.v("Logged in","Logged in");
                // Start sending presences
                //
                try {
                    chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                }

                // go to Dialogs screen
                //
                Intent intent = new Intent(context, MasterActivity.class);
                context.startActivity(intent);

               // context.finish();
            }

            @Override
            public void onError(List errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("chat login errors: " + errors).create().show();
            }
        });
    }

}
