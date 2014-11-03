package com.iiitd.mcproject;

import android.app.Activity;
import android.app.Fragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TrendingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TrendingFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TrendingFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ListView trendingTopics;
    private ArrayList<String> topicList=new ArrayList<String>();
    private ArrayList<String> imageList=new ArrayList<String>();

    TopicList adapter;
    String[] topicNames = {
            "Game of Thrones",
            "The Walking Dead",
            "Android",
            "Avengers",
            "India",
            "Google",
            "ISIS"
    } ;

    View inflateView;
    Context context;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrendingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrendingFragment newInstance(String param1, String param2) {
        TrendingFragment fragment = new TrendingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public TrendingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void addToList(){
        topicList.addAll(topicList);
        imageList.addAll(imageList);
        adapter.notifyDataSetChanged();
    }

    private void initList(){
        adapter = new TopicList(getActivity(), topicList, imageList);
        trendingTopics=(ListView)inflateView.findViewById(R.id.trending_list);
        trendingTopics.setAdapter(adapter);
        trendingTopics.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), "You Clicked at " + topicList.get(position), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity() , FreeBase.class);
                i.putExtra("topic" , topicList.get(position));
                startActivity(i);
            }
        });
        trendingTopics.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                if (trendingTopics.getLastVisiblePosition() == trendingTopics.getAdapter().getCount() - 1
                        && trendingTopics.getChildAt(trendingTopics.getChildCount() - 1).getBottom() <= trendingTopics.getHeight()) {
                    //scroll end reached
                    //Toast.makeText(getActivity(), "Reached end of list ", Toast.LENGTH_SHORT).show();
                    //add more items to list
                    addToList();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflateView= inflater.inflate(R.layout.fragment_trending, container, false);
        //getList();
        return inflateView;
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    //?maxwidth=225&maxheight=225&mode=fillcropmid


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onResume() {
        super.onResume();
        getList();
        getListImage();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void getList(){
        for (String topic : topicNames) {
            topicList.add(topic);
            imageList.add(null);
        }
        initList();
    }

    private void getListImage(){
        ConnectivityManager cmgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cmgr.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected()) {
            for (String topic : topicNames) {
                topicList.add(topic);
                try {
                    KnowledgeGraphTask(topic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }else{
            Toast.makeText(getActivity() , "No Internet Connection" , Toast.LENGTH_SHORT).show();
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

                if(topic_id == null){
                    return "ERROR";
                }else{
                    String image_id = "https://www.googleapis.com/freebase/v1/image" + getTopicImageId(topic_id) + "?maxwidth=200&maxheight=200&mode=fillcropmid"+ "&key="+Common.Freebase_api_key;
                    if(image_id == null){
                        return "ERROR";
                    }else if(topicList.indexOf(topic)>=0){
                        imageList.add(topicList.indexOf(topic),image_id);
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

                        return e.toString();
                    }
                }catch(JSONException e){
                    e.printStackTrace();

                    return e.toString();
                }catch (IOException e){
                    e.printStackTrace();
                    return e.toString();
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
