package com.iiitd.mcproject;

import android.app.Activity;
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

    public RailsServerSignUp(String a, String b, String c, String d, String e, Context cnt)
    {
        this.userLogin = a;
        this.userEmail = b;
        this.userPassword = c;
        this.usertokenQB = d;
        this.userQBId = e;
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
            Log.d("system response",str);
            getRailsToken(str);
        }
        else
            Log.d("system response","is null");
        userChat_auth();
    }

    private void getRailsToken(String str1)
    {
        try
        {
            Log.d("string",str1);
            str1 = str1.substring(1,str1.length()-1);
            String[] res = str1.split(",");
            for (String abc : res) {
                String[] pair = abc.split(":");
                if (pair[0].equals("\"id\""))
                    railsID = pair[1];
                if(pair[0].equals("\"token\""))
                {
                    railsToken = pair[1];
                    railsToken = railsToken.substring(1,railsToken.length()-1);
                }
            }
            Log.d("railsID",railsID);
            Log.d("railsToken",railsToken);
            SharedPreferences sharedPref;
            sharedPref = context.getSharedPreferences(Common.PREF, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("userRailsID",railsID);
            editor.putString("userRailsToken",railsToken);
            editor.commit();

        } catch (StringIndexOutOfBoundsException e) {
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
            Log.d("debug","userLogin:"+USER_LOGIN+" userPass:"+USER_PASSWORD);
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
                Log.d("debug" , "after loginToChat");
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("create session errors: " + errors).create().show();
            }
        });
    }

    private void loginToChat(final QBUser user){
        Log.d("user",user.toString());

        Log.d("debug","entered LoginToChat");
        chatService.login(user, new QBEntityCallbackImpl() {
            @Override
            public void onSuccess() {
                Log.d("Logged in","Logged in");
                // Start sending presences`
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
                Log.d("debug" , "started new activity");
//                ((Activity)context).finish();
                Log.d("debug" , "finished old activity");
            }

            @Override
            public void onError(List errors) {
                Log.d("debug" , "errors");
                Log.d("debug" , user.toString());
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setMessage("chat login errors: " + errors).create().show();
            }
        });
        Log.d("debug","exited LoginToChat");
    }
}
