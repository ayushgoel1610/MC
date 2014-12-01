package com.iiitd.mcproject;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.SeekBar;
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

import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * Created by shiv on 27/10/14.
 */


public class Topic extends Activity{

    //TEST COMMIT
    //TEST COMMIT 2

    private final String REPUTATION_TOPIC = "reputation";

    Button chat;
    ImageView image;
    ProgressBar bar ;
    TextView summary;
    EditText search_text;

    Switch check;

    SeekBar seek;

    String tag = new String("Topic");

    TopicObject chatTopic=new TopicObject();

    String image_id;
    String description;
    String text;
    String topic;
    int topic_id;
    int reputation;
    int user_id;


    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences pref = getSharedPreferences(Common.PREF, MODE_PRIVATE);
        Log.d("Int value", pref.getString("userRailsID", "null"));
        user_id = Integer.parseInt(pref.getString("userRailsID", "null"));

        setContentView(R.layout.topic);
        context=this;

        bar = (ProgressBar)findViewById(R.id.topic_search_progressBar);
        bar.setVisibility(View.INVISIBLE);
        summary = (TextView) findViewById(R.id.topic_search_text);
        summary.setVisibility(View.INVISIBLE);
        image = (ImageView)findViewById(R.id.topic_image);
        image.setVisibility(View.INVISIBLE);
//        check = (CheckBox) findViewById(R.id.chkIos);
        check = (Switch) findViewById(R.id.chklos);
        chat = (Button) findViewById(R.id.topic_chat);
        topic=getIntent().getStringExtra("topic");
        seek = (SeekBar) findViewById(R.id.topic_seekBar);

        topic_id = getIntent().getIntExtra("id", -1);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(topic);

        Log.d(tag , "Inside FreeBase Class");
                ConnectivityManager cmgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cmgr.getActiveNetworkInfo();
                if(networkInfo!=null && networkInfo.isConnected()){
                    bar.setVisibility(View.VISIBLE);
                    summary.setVisibility(View.INVISIBLE);
                    summary.setText("");
                    image.setVisibility(View.INVISIBLE);
                    image.setImageDrawable(null);
                    Log.d(tag, "Connected to internet");
                    TextView topicHeader=(TextView)findViewById(R.id.topicHeader);
                    topicHeader.setText(topic);
                    image_id = getIntent().getStringExtra("image");
                    Log.v(tag,"Image path: "+image_id);
                    setImage();
                    new KnowledgeGraphTask().execute(topic);
                    getReputation();
                }else{
                    Toast.makeText(getBaseContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setImage(){
        image.setVisibility(View.VISIBLE);

        final Resources res = context.getResources();
        final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        final LetterTileProvider tileProvider = new LetterTileProvider(context);
        final Bitmap letterTile = tileProvider.getLetterTile(topic, image_id, tileSize, tileSize);

        Drawable db=new BitmapDrawable(context.getResources(), letterTile);
        Picasso.with(getBaseContext()).load(image_id).fit().centerCrop().placeholder(db).error(db).into(image);
    }

    public void runChatClient(View view){
        Log.v("Chat pressed", "Chat pressed");
        Intent intent = new Intent(this, NewDialogActivity.class);
        intent.putExtra("id" , topic_id);
        intent.putExtra("topic" , topic);
        intent.putExtra("threshold" , seek.getProgress());
        int loc ;
        if(check.isChecked()){
            Log.d("debug", "location on");
            loc=1;
        }else{
            Log.d("debug", "location off");
            loc=0;
        }
        intent.putExtra("locflag" , loc);
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
            summary.setText(text);
            //summary.setMovementMethod(new ScrollingMovementMethod());
            summary.setVisibility(View.VISIBLE);
        }
    }

    private void getReputation(){
        new AsyncTask<Void , Void, Void>(){

            @Override
            protected void onPreExecute() {
                bar.setVisibility(View.VISIBLE);
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params) {
                getRep();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                seek.setMax(reputation);
                bar.setVisibility(View.INVISIBLE);
                super.onPostExecute(aVoid);
            }
        }.execute(null , null , null);
    }


    private void getRep()  {
        InputStream inputStream = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Common.SERVER_URL+REPUTATION_TOPIC);
        JSONObject jsonObject = new JSONObject();

        String json = new String();

        try {
            jsonObject.put("user_id" , user_id);
            jsonObject.put("topic_id" ,topic_id);
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

//        Log.v("Topic", Integer.toString(sl.getStatusCode()));

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
            reputation = Integer.parseInt(response.get("reputation").toString());
            Log.d("Topic" ,  "repuatation = " + reputation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}