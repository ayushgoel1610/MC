package com.iiitd.mcproject;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
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
    ProgressBar progress;
    TextView no_result;
    int resp_size = 0;

    private ArrayList<TopicObject> topicObjectList=new ArrayList<TopicObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        progress = (ProgressBar) findViewById(R.id.search_progressBar);
        progress.setVisibility(View.VISIBLE);
        query = getIntent().getStringExtra("topic");
        no_result = (TextView) findViewById(R.id.search_noresult_textView);
        no_result.setVisibility(View.INVISIBLE);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(query);
        initList();
        TopicTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        ComponentName cn = new ComponentName(this, SearchableActivity.class);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        searchView.setIconifiedByDefault(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void initList() {
        adapter = new TopicList(this, topicObjectList);
        trendingTopics = (ListView) findViewById(R.id.search_list);
        trendingTopics.setAdapter(adapter);
        trendingTopics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(getActivity(), "You Clicked at " + topicList.get(position), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplication(), Topic.class);
                TopicObject clickedTopic = topicObjectList.get(position);
                i.putExtra("topic", clickedTopic.getName());
                i.putExtra("id", clickedTopic.getId());
                i.putExtra("category", clickedTopic.getCategory());
                i.putExtra("image",clickedTopic.getImage());
                startActivity(i);
            }
        });
    }

    private void getListImage(){
        ConnectivityManager cmgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cmgr.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {
            for (TopicObject topicObject : topicObjectList)  {
                try {
                    KnowledgeGraphTask(topicObject);
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
                if(resp_size > 0) {
                    adapter.notifyDataSetChanged();
                    getListImage();
                }else {
                    Log.d("Search", "onPostExecute");
                    String dis = "Could not find topic " + "\"" + query + "\"";
                    no_result.setText(dis);
                    no_result.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.INVISIBLE);
                }
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
                resp_size = topicsArray.length();
                if(resp_size > 0) {
                    addNewTopics(topicsArray);
                }else{
                    Log.d("Search" , "No results found");
                }
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
                TopicObject topicObject=new TopicObject();
                topicObject.putId(JSONTopic.getInt("id"));
                topicObject.putCategory(JSONTopic.getString("category"));
                topicObject.putName(JSONTopic.getString("name"));
                topicObject.putImage("");
                topicObjectList.add(topicObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void KnowledgeGraphTask(final TopicObject topicObject)
    {
        new AsyncTask<Void , String , String>() {

            String tag = new String("KnowledgeGraphTask");

            @Override
            protected String doInBackground(Void... param) {
                Log.d(tag, "inside the KnowledgeGraphTask");
                Log.d(tag , "The topic is : " + topicObject.getName());
                String topic = topicObject.getName();
                topic = topic.replace(" " , "_");

                String category = topicObject.getCategory();
                category=category.replace(" ","_");

                String topic_id = getTopicId(topic);
                Log.d(tag , "The topic id is : " + topic_id );

                topic = topic.replace("_" , " ");

                if(topic_id == null){
                    return "ERROR";
                }else{

                    String image_id = "https://www.googleapis.com/freebase/v1/image"
                            + getTopicImageId(topic_id) + "?maxwidth=200&maxheight=200&mode=fillcropmid"
                            + "&key="+ Common.Freebase_api_key;
                    if(image_id.equals("https://www.googleapis.com/freebase/v1/imagenull")){

                        return "ERROR";
                    }else if(topicObjectList.indexOf(topicObject)>=0){
                        //imageList.add(topicList.indexOf(topic),image_id);
//                        Log.v(tag,"Adding image: "+image_id);
                        topicObjectList.get(topicObjectList.indexOf(topicObject)).putImage(image_id);
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
                progress.setVisibility(View.INVISIBLE);
                //Populate list
                try {
                    adapter.notifyDataSetChanged();
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        }.execute(null, null, null);
    }
}
