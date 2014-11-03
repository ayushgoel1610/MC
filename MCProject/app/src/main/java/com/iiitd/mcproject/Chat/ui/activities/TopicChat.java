package com.iiitd.mcproject.Chat.ui.activities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iiitd.mcproject.Common;
import com.iiitd.mcproject.R;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shiv on 4/11/14.
 */

public class TopicChat extends Activity{

    Timer mytimer ;
    int topic_id;
    private static final String CHAT_REQUEST="chats/request";
    private static final String CHAT_PAIR="chats/pair";

    TextView display ;


    int pair_status_count = 0;
    private String request_status = "" ;
    private String pair_status = "" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_chat);
        display = (TextView)findViewById(R.id.topic_chat_display);
        RequestTask();

        mytimer = new Timer();
        mytimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PairTask();
            }
        } , 0 ,5000);

        Button b = (Button) findViewById(R.id.topic_chat_pair);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PairTask();
            }
        });
    }

    private void RequestTask(){
        new AsyncTask<Void , Void , Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                requestPair();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                display.setText(request_status);
            }
        }.execute(null , null , null);
    }

    public void requestPair()  {
        InputStream inputStream = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Common.SERVER_URL+CHAT_REQUEST);
        JSONObject jsonObject = new JSONObject();

        String json = new String();

        try {
            jsonObject.put("user_id" , 1);
            jsonObject.put("topic_id" , 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        json=jsonObject.toString();

        StringEntity se = null;
        try {
            se = new StringEntity(json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

        httpPost.setEntity(se);
        org.apache.http.HttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream = httpResponse.getEntity().getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StatusLine sl=httpResponse.getStatusLine();

        Log.v("TopicChat", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase());

        StringBuffer sb=new StringBuffer();

        int ch;
        try {
            while ((ch = inputStream.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject response=new JSONObject(sb.toString());
            request_status = response.get("status").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void PairTask(){
        new AsyncTask<Void , Void , Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                findPair();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                display.setText(pair_status);
            }
        }.execute(null , null , null);
    }


    private void findPair()  {
        InputStream inputStream = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Common.SERVER_URL+CHAT_PAIR);
        JSONObject jsonObject = new JSONObject();

        String json = new String();

        try {
            jsonObject.put("user_id" , 1);
            jsonObject.put("topic_id" , 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        json=jsonObject.toString();

        StringEntity se = null;
        try {
            se = new StringEntity(json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

        httpPost.setEntity(se);
        org.apache.http.HttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream = httpResponse.getEntity().getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StatusLine sl=httpResponse.getStatusLine();

        Log.v("TopicChat", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase() + " FIND PAIR");

        StringBuffer sb=new StringBuffer();

        int ch;
        try {
            while ((ch = inputStream.read()) != -1) {
                sb.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONObject response=new JSONObject(sb.toString());
            pair_status = response.get("status").toString();
            Log.d("TopicChat" , pair_status + "  " + "IN") ;
            if(pair_status.equals("No users available") || pair_status.equals("No users available!Request expired.")){
                pair_status_count++;
                pair_status = response.get("status").toString() + " " + Integer.toString(pair_status_count);
                Log.d("TopicChat" , pair_status);
                Log.d("TopicChat" , Integer.toString(pair_status_count));
            }else{
                pair_status_count = 4;
                mytimer.cancel();
            }
            if(pair_status_count > 2){
                mytimer.cancel();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
