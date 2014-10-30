package com.iiitd.mcproject;

import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by shiv on 30/10/14.
 */


public class KnowledgeGraph extends AsyncTask<String , Void , byte[]> {

    String tag = new String("KnowledgeGraphTask");

    @Override
    protected byte[] doInBackground(String... param) {
        Log.d(tag, "inside the KnowledgeGraphTask");
        Log.d(tag , "The topic is : " + param[0]);
        String topic = new String(param[0]);
        topic = topic.replace(" " , "_");

        String topic_id = getTopicId(topic);
        Log.d(tag , "The topic id is : " + topic_id );

        if(topic_id == null){
            return null;
        }else{
            String image_id = getTopicImageId(topic_id);
            if(image_id == null){
                return null;
            }else{
                return getTopicImage(image_id);
            }
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
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag , "Something went wrong");
            return null;
        }
    }

    private String getTopicImageId(String topic_id){
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
                Log.d(tag, "Something went wrong");
                return null;
            }
        }catch(JSONException e){
            e.printStackTrace();
            Log.d(tag, "Something went wrong");
            return null;
        }catch (IOException e){
            e.printStackTrace();
            Log.d(tag, "Something went wrong");
            return null;
        }
    }

    private byte[] getTopicImage(String image_id){
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
            //Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
            Log.d(tag , "Image retrieved");
            return b;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag , "Something went wrong");
            return null;
        }
    }

    @Override
    protected void onPostExecute(byte[] s) {
        super.onPostExecute(s);
        Log.d(tag , "The result is : " + s);
    }
}