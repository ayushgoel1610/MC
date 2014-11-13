package com.iiitd.mcproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.users.model.QBUser;

import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jivesoftware.smack.SmackException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Shubham on 13 Nov 14.
 */
public class RailsServerSignUp extends AsyncTask <Void, Void, String>
{
    private String userLogin;
    private String userEmail;
    private String userFullName;
    private String userPassword;
    private String usertokenQB;
    private String userQBId;
    private Context context;

    private static String USER_LOGIN ;
    private static String USER_PASSWORD;

    private String railsToken;
    private String railsID;

    static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    private QBChatService chatService;

    private static final String APP_ID = "15476";
    private static final String AUTH_KEY = "GeO3pbwR999HM9w";
    private static final String AUTH_SECRET = "h9LvUs4uLShEG7S";

    private ProgressDialog pDialog;
    private final String UrlRails = "https://tranquil-stream-2635.herokuapp.com/users";
    private StringBuilder sb = new StringBuilder();

    private JSONObject userObj = new JSONObject();
    private JSONObject bigObj = new JSONObject();

    public RailsServerSignUp(String a, String b, String c, String d, String e, String f, Context cnt)
    {
        this.userLogin = a;
        this.userEmail = b;
        this.userFullName = c;
        this.userPassword = d;
        this.usertokenQB = e;
        this.userQBId = f;
        this.context = cnt;
    }

    public RailsServerSignUp(Context cnt)
    {
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
    protected String doInBackground(Void... params) {

        InputStream inputStream = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(UrlRails);

            String json = "";

            JSONObject jsonObject = new JSONObject();
            JSONObject outerJSON = new JSONObject();
            try {
                jsonObject.put("name", userLogin);
                jsonObject.put("email", userEmail);
                jsonObject.put("password", userPassword);
                jsonObject.put("password_confirmation", userPassword);
                jsonObject.put("chat_id", userQBId);
                outerJSON.put("user", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            json = outerJSON.toString();
            Log.d("json",json);


            StringEntity se = new StringEntity(json);
            //	        se.setContentType("application/json;charset=UTF-8");
//            se.setContentType(new BasicHeader("Accept", "application/json"));
//            se.setContentType(new BasicHeader("Content-type","application/json"));
            httpPost.setHeader("Accept","application/json");
            httpPost.setHeader("Content-type","application/json");
            httpPost.setEntity(se);


            org.apache.http.HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
            StatusLine sl = httpResponse.getStatusLine();

            Log.v("debug", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase());

            StringBuffer sb = new StringBuffer();

            try {
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    sb.append((char) ch);
                }
                Log.d("system response", sb.toString());
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
        getRailsToken(str);
        userChat_auth();
        if(str != null)
        {
            Log.d("system response",str);
        }
        else
            Log.d("system response","is null");
    }

    private void getRailsToken(String string)
    {
        try {
            JSONObject json = new JSONObject(string);
            railsID=json.getString("id");
            railsToken=json.getString("token");
            Log.d("railsID",railsID);
            Log.d("railsToken",railsToken);
            SharedPreferences sharedPref;
            sharedPref = context.getSharedPreferences(Common.PREF, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("userRailsID",railsID);
            editor.putString("userRailsToken",railsToken);
            editor.commit();
            Log.d("sharedPref",sharedPref.getString("userRailsID","null"));
            Log.d("sharedPref",sharedPref.getString("userRailsToken","null"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void userChat_auth(){
        SharedPreferences sp = context.getSharedPreferences(Common.PREF, context.MODE_PRIVATE);
        this.USER_LOGIN = sp.getString("userLogin","null");
        this.USER_PASSWORD = sp.getString("userPassword","null");
        if(USER_PASSWORD.equals("null") || USER_LOGIN.equals("null"))
            Log.d("debug","user login and password are null");
        else
            Log.d("debug","userLogon:"+USER_LOGIN+" userPass:"+USER_PASSWORD);
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
                Log.d("debug", "on success");

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
