package com.iiitd.mcproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.api.client.json.Json;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
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
import java.sql.Array;
import java.util.ArrayList;
import java.util.Random;


public class ProfileActivity extends Activity {

    ProgressBar progress;
    GraphicalView category_graph = null;
    MultipleCategorySeries mreputationSeries;
    DefaultRenderer reputationRenderer;
    GraphicalView graph;
    LinearLayout graph_layout;
    JSONObject server_resp;
    JSONArray reputation;
    ArrayList<String> category = new ArrayList<String>();
    ArrayList<Double> category_reputation = new ArrayList<Double>();
    TextView email;
    TextView username;
    TextView mvp;
    String details = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        SharedPreferences sp = this.getSharedPreferences(Common.PREF, Context.MODE_PRIVATE);
        String railsid=sp.getString("userRailsID","null");
        Log.d("ID",railsid);

        getActionBar().setTitle("Spalk");

        progress = (ProgressBar) findViewById(R.id.profile_progressBar);
        progress.setVisibility(View.VISIBLE);
        graph_layout = (LinearLayout) findViewById(R.id.profile_linearLayout);

        username = (TextView) findViewById(R.id.profile_username_textView);
        username.setText(sp.getString("userLogin" , null));
        email = (TextView) findViewById(R.id.profile_email_textView);
        email.setText(sp.getString("userEmail", null));

        mvp = (TextView) findViewById(R.id.profile_details_textView);

        FetchProfile fetchProfile=new FetchProfile(railsid,ProfileActivity.this);
        fetchProfile.execute();
    }


    private void profile_chart(){

        mreputationSeries = new MultipleCategorySeries("Reputation");

        reputationRenderer = new DefaultRenderer();
        reputationRenderer.setShowLabels(true);
        reputationRenderer.setInScroll(true);
        reputationRenderer.setPanEnabled(false);// Disable User Interaction
        reputationRenderer.setScale((float) 1.4);
        reputationRenderer.setZoomEnabled(true);
        reputationRenderer.setPanEnabled(false);
        reputationRenderer.setLabelsTextSize(40);
        reputationRenderer.setLabelsColor(Color.BLACK);


        String cat[] = new String[category.size()];
        double rat[] = new double[category.size()];

        Random random = new Random();
        int[] colors = new int[category.size()];
        for(int i = 0 ; i < category.size() ;i++){
            colors[i] = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        for(int i = 0 ; i < category.size() ; i++){
            cat[i] = category.get(i);
            rat[i] = category_reputation.get(i);
        }

        for(int i = 0 ;i<category.size();i++){
            SimpleSeriesRenderer seriesRenderer = new SimpleSeriesRenderer();
            seriesRenderer.setColor(colors[i]);
            reputationRenderer.addSeriesRenderer(seriesRenderer);
        }

        mreputationSeries.add(cat , rat);
        graph = ChartFactory.getDoughnutChartView(this , mreputationSeries , reputationRenderer);
        graph_layout.addView(graph);
    }


    private void parseResp(JSONObject jsonObject){
        Log.d("ProfileActivity" , jsonObject.toString());
        try {
            JSONArray jcategories = jsonObject.getJSONArray("categories");
            Log.d("ProfileActivity" , jcategories.toString());
            for(int i = 0 ; i < jcategories.length() ; i++){
                JSONObject json = new JSONObject();
                json = jcategories.getJSONObject(i);
                Log.d("ProfileActivity" , json.getString("category"));
                category.add(json.getString("category"));
                category_reputation.add(json.getDouble("reputation"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            JSONObject mct = jsonObject.getJSONObject("mostchats");
            details = details + "MCT : " + mct.getString("topic_name") + "   Chats : " +  mct.getInt("chats") + "\n";
            JSONObject mrt = jsonObject.getJSONObject("mvptopic");
            details = details + "MRT : " + mrt.getString("topic_name") +  "  Rep  : " + mrt.getInt("reputation")  + "\n";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("ProfileActivity" , Integer.toString(category.size()));
    }

    private class FetchProfile extends AsyncTask<Void, Void, Void> {
        String id;
        private final String UrlRails = Common.SERVER_URL+"profile";
        private Context context;
        private StringBuilder sb=new StringBuilder();
        public FetchProfile(String id,Context cnt){
            this.id=id;
            context=cnt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            InputStream inputStream = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(UrlRails);
            JSONObject jsonObject = new JSONObject();

            String json = new String();

            try {
                jsonObject.put("id" , id);
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
            StringBuffer sb=new StringBuffer();

            Log.d("ProfileActivity", Integer.toString(sl.getStatusCode()));

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
                parseResp(response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progress.setVisibility(View.INVISIBLE);
            profile_chart();
            mvp.setText(details);
            super.onPostExecute(aVoid);
        }
    }
}
