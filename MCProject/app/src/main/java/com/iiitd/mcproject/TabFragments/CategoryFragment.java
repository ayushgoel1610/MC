package com.iiitd.mcproject.TabFragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.iiitd.mcproject.Common;
import com.iiitd.mcproject.R;

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
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CategoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CategoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ListView categoryListView;
    private ArrayList<String> categoryList=new ArrayList<String>();
    ArrayAdapter<String> adapter;
    View inflateView;
    String categoryArray[];
    String tag = new String("getCategoryTask");

    private static final String CATEGORY_LIST_API="topics/categories/";


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoryFragment newInstance(String param1, String param2) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public CategoryFragment() {
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

    private void initList(){
        categoryArray=new String[categoryList.size()];
        categoryList.toArray(categoryArray);
        adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, android.R.id.text1,categoryArray);
        categoryListView=(ListView)inflateView.findViewById(R.id.categoryListView);
        categoryListView.setAdapter(adapter);
        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getActivity(), "You Clicked at " + categoryList.get(position), Toast.LENGTH_SHORT).show();
                //Intent i = new Intent(getActivity() , Topic.class);

                //startActivity(i);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflateView= inflater.inflate(R.layout.fragment_category, container, false);
        getList();
        return inflateView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

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

    private void getList(){
        CategoryTask();
        //initList();
    }

    private void CategoryTask(){
        new AsyncTask<Void, String, String>(){

            @Override
            protected String doInBackground(Void... param) {
                String result=getTopics();
                return result;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(tag, msg);
                if(msg.contains("retrieved")) {
                    try {
                            initList();
                        }
                        catch (NullPointerException e){
                            e.printStackTrace();
                        }

                }
                //Populate list
                //adapter.notifyDataSetChanged();
            }
        }.execute(null, null, null);
    }

    private String getTopics(){
        InputStream inputStream = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(Common.SERVER_URL+CATEGORY_LIST_API);

            String json = "";

            JSONObject jsonObject = new JSONObject();

            json = jsonObject.toString();
            Log.e(tag,json);

            StringEntity se = new StringEntity(json);
            //	        se.setContentType("application/json;charset=UTF-8");
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

            httpPost.setEntity(se);

            org.apache.http.HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();
            StatusLine sl=httpResponse.getStatusLine();

            Log.v(tag, Integer.toString(sl.getStatusCode())+" "+sl.getReasonPhrase());

            StringBuffer sb=new StringBuffer();

            try {
                int ch;
                while ((ch = inputStream.read()) != -1) {
                    sb.append((char) ch);
                }
                Log.v("ELSERVICES", "first input stream: "+sb.toString());

                JSONObject response=new JSONObject(sb.toString());
                JSONArray categoriesArray=response.getJSONArray("list");
                addNewCategories(categoriesArray);

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

    private void addNewCategories(JSONArray categoriesArray){
       for(int i=0;i<categoriesArray.length();i++){
            try {
               categoryList.add(categoriesArray.getString(i));
                Log.v(tag,categoriesArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        categoryArray=new String[categoryList.size()];
        categoryList.toArray(categoryArray);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

}
