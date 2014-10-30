package com.iiitd.mcproject;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;


/**
 * Created by shiv on 27/10/14.
 */


public class FreeBase extends Activity{


    Bitmap bmp ;

    ImageView image;
    ProgressBar bar ;
    TextView summary;
    EditText search_text;

    String api_key = new String("AIzaSyBYeRWNMW3EHasjpawd1cSJ0cUOdInM6ds");
    String tag = new String("FreeBase class");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic);

        Button search = (Button)findViewById(R.id.topic_search_button);
        bar = (ProgressBar)findViewById(R.id.topic_search_progressBar);
        bar.setVisibility(View.INVISIBLE);
        summary = (TextView) findViewById(R.id.topic_search_text);
        summary.setVisibility(View.INVISIBLE);
        search_text = (EditText)findViewById(R.id.topic_search_editText);
        image = (ImageView)findViewById(R.id.topic_image);
        image.setVisibility(View.INVISIBLE);

        Log.d(tag , "Inside FreeBase Class");

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager cmgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cmgr.getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isConnected()){
                    if(search_text.getText().toString() != null && !search_text.getText().toString().isEmpty()) {
                        bar.setVisibility(View.VISIBLE);
                        summary.setVisibility(View.INVISIBLE);
                        summary.setText("");
                        image.setVisibility(View.INVISIBLE);
                        image.setImageDrawable(null);
                        bmp = null;
                        Log.d(tag, "Connected to internet");
                        try {
                            String res = new KnowledgeGraphTask().execute(search_text.getText().toString()).get();
                          //String res = new KnowledgeGraphTask().execute("inception").get();
                            if(bmp == null){
                                Toast.makeText(getBaseContext() , "No Image Found" , Toast.LENGTH_SHORT).show();
                            }else {
                                image.setImageBitmap(bmp);
                            }
                            Log.d(tag, "Back to FreeBase : " + res);
                            summary.setText(res);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(getBaseContext() , "please fill in the search value " , Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class KnowledgeGraphTask extends AsyncTask<String , Void , String>{

        @Override
        protected String doInBackground(String... param) {
            Log.d(tag , "inside the KnowledgeGraphTask");
            Log.d(tag , "The topic is : " + param[0]);
            String topic = new String(param[0]);
            topic = topic.replace(" " , "_");

            String topic_id = getTopicId(topic);
            Log.d(tag , "The topic id is : " + topic_id );

            if(topic_id == null){
                return null;
            }else{
                return getTopicData(topic_id);
            }
        }

        private String getTopicId(String topic) {
            Log.d(tag , "Inside getTopicId");
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            String req_url = "https://www.googleapis.com/freebase/v1/search/?query=" + topic;
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
                Log.d(tag , json.getString("result"));
                Log.d(tag , "from json_array : "  + json_array.get(0).toString());
                JSONObject temp = (JSONObject)json_array.get(0);
                Log.d(tag , "the json object is : " + temp.toString());
                String topic_id = temp.getString("id");
                Log.d(tag , "the topic id of the most relevant search is : " + topic_id);
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

        private String getTopicData(String topic_id){
            Log.d(tag , "Inside getTopicData");
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            //String req_url = "https://www.googleapis.com/freebase/v1/topic" + topic_id;
            String req_url = "https://www.googleapis.com/freebase/v1/topic" + topic_id + "?filter=/common/topic/description&filter=/common/topic/image" ;
            Log.d(tag , "The topic_id url is : " + req_url);
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
            try {
                json = new JSONObject(httpResponse.parseAsString());
                Log.d(tag ,  "the json received : " + json.toString());
                JSONObject json_property = new JSONObject(json.getString("property"));
                Log.d(tag, "the json_property : " + json_property.toString());
                JSONObject json_description = new JSONObject(json_property.getString("/common/topic/description"));
                Log.d(tag ,  "the json_description : " + json_description.toString());
                JSONArray json_description_values = new JSONArray(json_description.getString("values"));
                Log.d(tag ,  "the json_description_values : " + json_description_values.toString());
                JSONObject json_value = (JSONObject)json_description_values.get(0);
                String description = json_value.getString("value");
                String text = json_value.getString("text");
                Log.d(tag , description);

                try {
                    JSONObject json_image = new JSONObject(json_property.getString("/common/topic/image"));
                    Log.d(tag, "the json_image : " + json_image.toString());
                    JSONArray json_image_values = new JSONArray(json_image.getString("values"));
                    Log.d(tag, "the json_image_values : " + json_image_values.toString());
                    JSONObject image = (JSONObject) json_image_values.get(0);
                    String image_id = image.getString("id");
                    Log.d(tag, "The image id is : " + image_id);

                    if (image_id != null) {
                        getTopicImage(image_id);
                    }
                }  catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(tag , "Something went wrong");
                }
                return text;
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

        private void getTopicImage(String image_id){
            Log.d(tag , "Inside getImageId");
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            String req_url = "https://www.googleapis.com/freebase/v1/image" + image_id;
            Log.d(tag , "The image_id url is : " + req_url);
            GenericUrl url = new GenericUrl(req_url);
            HttpRequest request = null;
            try {
                request = requestFactory.buildGetRequest(url);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return;
            }
            HttpResponse httpResponse = null;
            try {
                httpResponse = request.execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
                return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bufferSize = 1024;
            int len = 0;
            byte[] buffer = new byte[bufferSize];
            try {
                InputStream instream = httpResponse.getContent();
                while ((len = instream.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                baos.close();
                byte[] b = baos.toByteArray();
                bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
                Log.d(tag , "Image retrieved");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(tag , "Something went wrong");
            }
    }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(tag , "The result is : " + s);
            bar.setVisibility(View.INVISIBLE);
            summary.setVisibility(View.VISIBLE);
            image.setVisibility(View.VISIBLE);
        }
    }
}