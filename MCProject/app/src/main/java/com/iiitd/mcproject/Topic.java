package com.iiitd.mcproject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.iiitd.mcproject.Chat.ui.activities.NewDialogActivity;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * Created by shiv on 27/10/14.
 */


public class Topic extends Activity{

    //TEST COMMIT
    //TEST COMMIT 2

    ImageView image;
    ProgressBar bar ;
    TextView summary;
    EditText search_text;

    String tag = new String("Topic");

    String image_id;
    String description;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic);

        bar = (ProgressBar)findViewById(R.id.topic_search_progressBar);
        bar.setVisibility(View.INVISIBLE);
        summary = (TextView) findViewById(R.id.topic_search_text);
        summary.setVisibility(View.INVISIBLE);
        image = (ImageView)findViewById(R.id.topic_image);
        image.setVisibility(View.INVISIBLE);

        //Toast.makeText(this , "the topic id is : " + Integer.toString(getIntent().getIntExtra("id" , -1)) , Toast.LENGTH_SHORT).show();

        Log.d(tag , "Inside FreeBase Class");
                ConnectivityManager cmgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cmgr.getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isConnected()){
                        bar.setVisibility(View.VISIBLE);
                        summary.setVisibility(View.INVISIBLE);
                        summary.setText("");
                        image.setVisibility(View.INVISIBLE);
                        image.setImageDrawable(null);
                        image_id = null;
                        Log.d(tag, "Connected to internet");
                        new KnowledgeGraphTask().execute(getIntent().getStringExtra("topic"));
                }else{
                    Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }


    public void runChatClient(View view){
        Intent intent = new Intent(this, NewDialogActivity.class);
        intent.putExtra("id" , getIntent().getIntExtra("id" , -1));
        startActivity(intent);
    }

    private class KnowledgeGraphTask extends AsyncTask<String , Void , Void>{

        @Override
        protected Void doInBackground(String... param) {
            Log.d(tag , "inside the KnowledgeGraphTask");
            Log.d(tag , "The topic is : " + param[0]);
            String topic = new String(param[0]);
            topic = topic.replace(" " , "_");
            String topic_id = getTopicId(topic);
            Log.d(tag , "The topic id is : " + topic_id );
            if(topic_id != null){
                getTopicData(topic_id);
            }
            return null;
        }

        private String getTopicId(String topic) {
            Log.d(tag , "Inside getTopicId");
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            String req_url = "https://www.googleapis.com/freebase/v1/search/?query=" + topic + "&key="+Common.Freebase_api_key;
            Log.d(tag , "The search topic url is : " + req_url);
            GenericUrl url = new GenericUrl(req_url);
            HttpRequest request = null;
            try {
                request = requestFactory.buildGetRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return null;
            }
            HttpResponse httpResponse = null;
            try {
                httpResponse = request.execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return null;
            }
            JSONObject json = null;
            JSONArray json_array = null;
            try {
                json = new JSONObject(httpResponse.parseAsString());
                Log.d(tag, "The json retrieved is : " + json.toString());
                json_array = new JSONArray(json.getString("result"));
                //Log.d(tag , json.getString("result"));
                //Log.d(tag , "from json_array : "  + json_array.get(0).toString());
                JSONObject temp = (JSONObject)json_array.get(0);
                //Log.d(tag , "the json object is : " + temp.toString());
                String topic_id = temp.getString("id");
                //Log.d(tag , "the topic id of the most relevant search is : " + topic_id);
                return topic_id;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return null;
            }
        }

        private void getTopicData(String topic_id){
            Log.d(tag , "Inside getTopicData");
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            //String req_url = "https://www.googleapis.com/freebase/v1/topic" + topic_id;
            String req_url = "https://www.googleapis.com/freebase/v1/topic" + topic_id + "?filter=/common/topic/description&filter=/common/topic/image" + "&key="+Common.Freebase_api_key ;
            Log.d(tag , "The topic_id url is : " + req_url);
            GenericUrl url = new GenericUrl(req_url);
            HttpRequest request = null;
            try {
                request = requestFactory.buildGetRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return ;
            }
            HttpResponse httpResponse = null;
            try {
                httpResponse = request.execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return;
            }
            JSONObject json = null;
            try {
                json = new JSONObject(httpResponse.parseAsString());
                //Log.d(tag ,  "the json received : " + json.toString());
                JSONObject json_property = new JSONObject(json.getString("property"));
                //Log.d(tag, "the json_property : " + json_property.toString());
                JSONObject json_description = new JSONObject(json_property.getString("/common/topic/description"));
                //Log.d(tag ,  "the json_description : " + json_description.toString());
                JSONArray json_description_values = new JSONArray(json_description.getString("values"));
                //Log.d(tag ,  "the json_description_values : " + json_description_values.toString());
                JSONObject json_value = (JSONObject)json_description_values.get(0);
                description = json_value.getString("value");
                text = json_value.getString("text");
                //Log.d(tag , description);

                try {
                    JSONObject json_image = new JSONObject(json_property.getString("/common/topic/image"));
                    //Log.d(tag, "the json_image : " + json_image.toString());
                    JSONArray json_image_values = new JSONArray(json_image.getString("values"));
                    //Log.d(tag, "the json_image_values : " + json_image_values.toString());
                    JSONObject image = (JSONObject) json_image_values.get(0);
                    image_id = image.getString("id");
                    //Log.d(tag, "The image id is : " + image_id);
                }  catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(tag , "Something went wrong");
                }
                return;
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            bar.setVisibility(View.INVISIBLE);
            summary.setText(description);
            summary.setMovementMethod(new ScrollingMovementMethod());
            summary.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
            String imageUrl = "https://www.googleapis.com/freebase/v1/image" + image_id + "?maxwidth=500&maxheight=500&mode=fillcropmid" + "&key="+Common.Freebase_api_key;
            Picasso.with(getBaseContext()).load(imageUrl).error(R.drawable.ic_launcher).into(image);
        }
    }
}