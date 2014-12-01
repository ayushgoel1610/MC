package com.iiitd.mcproject;

import android.app.ProgressDialog;
import android.content.Context;
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
 * Created by udayantandon on 01/12/14.
 */
public class SendSuggestion extends AsyncTask<Void,Void,String> {
    String topic;
    String category;
    private ProgressDialog pDialog;
    private final String UrlRails = Common.SERVER_URL+"suggestion";
    private Context context;
    private StringBuilder sb=new StringBuilder();
    public SendSuggestion(String topic,String category,Context cnt){
        this.topic=topic;
        this.category=category;
        context=cnt;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Submitting Suggestion");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(Void... voids) {
        InputStream inputStream=null;
        try{
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(UrlRails);
            String json="";

            JSONObject outerJSON = new JSONObject();
            JSONObject innerJSON=new JSONObject();
            try {
                innerJSON.put("name",topic);
                innerJSON.put("category",category);
                outerJSON.put("suggestion",innerJSON);

            }catch (JSONException e){
                e.printStackTrace();
            }
            json = outerJSON.toString();
            Log.d("json", json);

            StringEntity se = new StringEntity(json);
            httpPost.setHeader("Accept","application/json");
            httpPost.setHeader("Content-type","application/json");
            httpPost.setEntity(se);

            org.apache.http.HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
            StatusLine sl = httpResponse.getStatusLine();
            Log.v("debug", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase());

            try {
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    sb.append((char) ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }

        }catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    protected void onPostExecute(String str)
    {
        pDialog.dismiss();
        if(str != null)
        {
            Log.d("system response in suggestion",str);
        }
        else
            Log.d("system response","is null");
    }
}
