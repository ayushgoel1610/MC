package com.iiitd.mcproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

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
import java.util.ArrayList;

/**
 * Created by shiv on 13/11/14.
 */
public class Search extends Activity{

    ListView trendingTopics;
    private static final String TOPIC_SEARCH = "/topics/search";
    String query;
    TopicList adapter;

    private ArrayList<String> topicList=new ArrayList<String>();
    private ArrayList<Integer> topicIDList=new ArrayList<Integer>();
    private ArrayList<String> imageList=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        query = getIntent().getStringExtra("topic");
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(query);
        initList();
        TopicTask();
    }


    private void initList() {
        adapter = new TopicList(this, topicList, imageList);
        trendingTopics = (ListView) findViewById(R.id.search_list);
        trendingTopics.setAdapter(adapter);
        trendingTopics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(getActivity(), "You Clicked at " + topicList.get(position), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplication(), Topic.class);
                i.putExtra("topic", topicList.get(position));
                i.putExtra("id", topicIDList.get(position));
                int p = topicIDList.get(position);
                Log.d("SearchActivity", "The topic id is : " + Integer.toString(topicIDList.get(position)));
                startActivity(i);
            }
        });
    }


    private void getListImage(){
        ConnectivityManager cmgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cmgr.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {

            for (String topic : topicList) {
                try {
                    KnowledgeGraphTask(topic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            Toast.makeText(this , "No Internet Connection" , Toast.LENGTH_SHORT).show();
        }

    }

    private void TopicTask(){
        new AsyncTask<Void, String, String>(){

            @Override
            protected String doInBackground(Void... param) {
                String result=getTopics(query);
                return result;
            }

            @Override
            protected void onPostExecute(String msg) {
                adapter.notifyDataSetChanged();
                getListImage();
            }
        }.execute(null, null, null);
    }

    private String getTopics(String query){
        InputStream inputStream = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(Common.SERVER_URL+TOPIC_SEARCH);

            String json = "";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("query", query);
            } catch (JSONException e) {
                e.printStackTrace();
                throw e;
            }

            json = jsonObject.toString();
            Log.d("SearchActivity",json);

            StringEntity se = new StringEntity(json);

            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

            httpPost.setEntity(se);

            org.apache.http.HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
            StatusLine sl=httpResponse.getStatusLine();

            Log.d("SearchActivity", Integer.toString(sl.getStatusCode())+" "+sl.getReasonPhrase());

            StringBuffer sb=new StringBuffer();

            try {
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    sb.append((char) ch);
                }

                JSONObject response=new JSONObject(sb.toString());
                JSONArray topicsArray=response.getJSONArray("list");
                addNewTopics(topicsArray);

            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }

        }
        catch (Exception e){
            return "ERROR : Due to "+e.getMessage();
        }
        return "topics retrieved successfully";
    }

    private void addNewTopics(JSONArray topicsArray){
        JSONObject JSONTopic;
        for(int i=0;i<topicsArray.length();i++){
            try {
                JSONTopic=topicsArray.getJSONObject(i);
                String topic=JSONTopic.getString("name");
                Log.d("SearchActivity" , topic);
                topicIDList.add(Integer.parseInt(JSONTopic.getString("id")));
                topicList.add(topic);
                imageList.add(null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void KnowledgeGraphTask(final String topicName)
    {
        new AsyncTask<Void , String , String>() {

            String tag = new String("KnowledgeGraphTask");

            @Override
            protected String doInBackground(Void... param) {
                Log.d(tag, "inside the KnowledgeGraphTask");
                Log.d(tag , "The topic is : " + topicName);
                String topic = topicName;
                topic = topic.replace(" " , "_");

                String topic_id = getTopicId(topic);
                Log.d(tag , "The topic id is : " + topic_id );

                topic = topic.replace("_" , " ");

                if(topic_id == null){
                    return "ERROR";
                }else{

                    String image_id = "https://www.googleapis.com/freebase/v1/image" + getTopicImageId(topic_id) + "?maxwidth=200&maxheight=200&mode=fillcropmid"+ "&key="+ Common.Freebase_api_key;
                    if(image_id.equals("https://www.googleapis.com/freebase/v1/imagenull")){
                        return "ERROR";
                    }else if(topicList.indexOf(topic)>=0){
                        imageList.add(topicList.indexOf(topic),image_id);
//                        Log.v(tag,"Adding image: "+image_id);
                        return image_id;
                    }
                }
                return topic;
            }

            private String getTopicId(String topic) {
                Log.d(tag , "Inside getTopicId");
                HttpTransport httpTransport = new NetHttpTransport();
                HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
                String req_url = "https://www.googleapis.com/freebase/v1/search/?query=" + topic+ "&key="+Common.Freebase_api_key;
                Log.d(tag , "The search topic url is : " + req_url);
                GenericUrl url = new GenericUrl(req_url);
                HttpRequest request = null;
                try {
                    request = requestFactory.buildGetRequest(url);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(tag , "Something went wrong");
                    return e.toString();
                }
                HttpResponse httpResponse = null;
                try {
                    httpResponse = request.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(tag , "Something went wrong");
                    return e.toString();
                }
                JSONObject json = null;
                JSONArray json_array = null;
                try {
                    json = new JSONObject(httpResponse.parseAsString());
                    //Log.d(tag, "The json retrieved is : " + json.toString());
                    json_array = new JSONArray(json.getString("result"));
                    //Log.d(tag , json.getString("result"));
                    //Log.d(tag , "from json_array : "  + json_array.get(0).toString());
                    JSONObject temp = (JSONObject)json_array.get(0);
                    Log.d(tag , "the json object is : " + temp.toString());
                    String topic_id = temp.getString("id");
                    //Log.d(tag , "the topic id of the most relevant search is : " + topic_id);
                    return topic_id;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(tag , "Something went wrong");
                    return e.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(tag , "Something went wrong");
                    return e.toString();
                }
            }

            private String getTopicImageId(String topic_id){
                Log.d(tag , "Inside getTopicData");
                HttpTransport httpTransport = new NetHttpTransport();
                HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
                //String req_url = "https://www.googleapis.com/freebase/v1/topic" + topic_id;
                String req_url = "https://www.googleapis.com/freebase/v1/topic" + topic_id + "?filter=/common/topic/description&filter=/common/topic/image"+ "&key="+Common.Freebase_api_key ;
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
                    //Log.d(tag ,  "the json received : " + json.toString());
                    JSONObject json_property = new JSONObject(json.getString("property"));
                    //Log.d(tag, "the json_property : " + json_property.toString());
                    JSONObject json_description = new JSONObject(json_property.getString("/common/topic/description"));
                    //Log.d(tag ,  "the json_description : " + json_description.toString());
                    JSONArray json_description_values = new JSONArray(json_description.getString("values"));
                    //Log.d(tag ,  "the json_description_values : " + json_description_values.toString());
                    JSONObject json_value = (JSONObject) json_description_values.get(0);
                    String description = json_value.getString("value");
                    String text = json_value.getString("text");
                    //Log.d(tag , description);
                    try {
                        JSONObject json_image = new JSONObject(json_property.getString("/common/topic/image"));
                        //Log.d(tag, "the json_image : " + json_image.toString());
                        JSONArray json_image_values = new JSONArray(json_image.getString("values"));
                        //Log.d(tag, "the json_image_values : " + json_image_values.toString());
                        JSONObject image = (JSONObject) json_image_values.get(0);
                        String image_id = image.getString("id");
                        Log.d(tag, "The image id is : " + image_id);
                        return image_id;
                    } catch (JSONException e) {
                        e.printStackTrace();

                        return null;
                    }
                }catch(JSONException e){
                    e.printStackTrace();

                    return e.toString();
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(tag, msg);
                //Populate list
                adapter.notifyDataSetChanged();
            }
        }.execute(null, null, null);
    }

}
