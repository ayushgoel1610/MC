package com.iiitd.mcproject.Chat.ui.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iiitd.mcproject.Chat.ui.activities.ChatActivity;
import com.iiitd.mcproject.Chat.ui.adapters.UsersAdapter;
import com.iiitd.mcproject.Common;
import com.iiitd.mcproject.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

//import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class UsersFragment extends Fragment implements QBEntityCallback<ArrayList<QBUser>> {

    private static final String CHAT_REQUEST="chats/request";
    private static final String CHAT_PAIR="chats/pair";

    TextView display ;
    Button retry ;


    int topic_id;           //Topic ID
    int user_id;            //User quickblocks id , retrieved from SharedPref
    int pair_id;             //Other user quikcblocks id , getting from my Rails server.
    int chat;               //The chat_id at my rails server, use this to send the reputation


    int pair_status_count = 0;
    private String request_status = "" ;
    private String pair_status = "" ;

    private static final int PAGE_SIZE = 10;
    //private PullToRefreshListView usersList;
    private Button createChatButton;
    private int listViewIndex;
    private int listViewTop;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;

    private int currentPage = 0;
    private List<QBUser> users = new ArrayList<QBUser>();

    private Handler chatHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            QuickBlocksChat();
        }
    };

    private Handler pairHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            PairTask();
        }
    };

    public static UsersFragment getInstance() {
        return new UsersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF, getActivity().MODE_PRIVATE);
        Log.d("Int value", pref.getString("userRailsID", "null"));
        user_id = Integer.parseInt(pref.getString("userRailsID", "null"));
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        display = (TextView)v.findViewById(R.id.user_fragment_status);
        display.setVisibility(View.INVISIBLE);
        retry = (Button) v.findViewById(R.id.retry_chat_button);
        retry.setVisibility(View.INVISIBLE);
        Log.d("UserFragment" , "The topic id is : " + Integer.toString(getActivity().getIntent().getIntExtra("id" , -1)));
        topic_id = getActivity().getIntent().getIntExtra("id" , -1);


       Request();

//        QuickBlocksChat();

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request();
            }
        });
        return v;
    }

    public void Request(){
        progressBar.setVisibility(View.VISIBLE);
        display.setVisibility(View.INVISIBLE);
        retry.setVisibility(View.INVISIBLE);
        pair_status_count = 0;
        RequestTask();
    }

    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }

    @Override
    public void onSuccess(ArrayList<QBUser> newUsers, Bundle bundle){

    }

    @Override
    public void onSuccess(){

    }

    @Override
    public void onError(List<String> errors){
        AlertDialog.Builder dialog = new AlertDialog.Builder(UsersFragment.getInstance().getActivity());
        dialog.setMessage("get users errors: " + errors).create().show();
    }

    private String usersListToChatName(){
        String chatName = "";
        for(QBUser user : usersAdapter.getSelected()){
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getLogin();
        }
        Log.v("chatName", chatName);
        return chatName;
    }

    public void startSingleChat(QBDialog dialog) {
        Log.v("other stuff", "other stuff");
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.PRIVATE);
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);
        bundle.putInt("pair_id",pair_id);
        ChatActivity.start(getActivity(), bundle);
    }

    private void startGroupChat(QBDialog dialog){

        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_DIALOG, dialog);
        bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.GROUP);

        ChatActivity.start(getActivity(), bundle);
    }

    private void loadNextPage() {
        ++currentPage;

        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), UsersFragment.this);
    }

    public static ArrayList<Integer> getUserIds(List<QBUser> users){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(QBUser user : users){
            ids.add(user.getId());
        }
        return ids;
    }

    private void RequestTask(){
        new AsyncTask<Void , Void , Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                requestPair();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                display.setText(request_status);
                //progressBar.setVisibility(View.INVISIBLE);
                //display.setVisibility(View.VISIBLE);
                Message msg = new Message();
                msg.arg1 = 1;
                pairHandler.sendMessage(msg);
            }
        }.execute(null , null , null);
    }

    public void requestPair()  {
        InputStream inputStream = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Common.SERVER_URL+CHAT_REQUEST);
        JSONObject jsonObject = new JSONObject();

        String json = new String();

        try {
            jsonObject.put("user_id" , user_id);
            jsonObject.put("topic_id" , topic_id);
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

        Log.v("TopicChat", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase());

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
            request_status = response.get("status").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void PairTask(){
        new AsyncTask<Void , Void , Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                findPair();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(pair_status.equals("user found")){
                    progressBar.setVisibility(View.INVISIBLE);
                    display.setText(pair_status);
                    display.setVisibility(View.VISIBLE);
                    Message msg = new Message();
                    msg.arg1 = 1;
                    chatHandler.sendMessage(msg);
                    return;
                }
                if(pair_status_count < 3){
                    Message msg = new Message();
                    msg.arg1 = 1;
                    pairHandler.sendMessage(msg);
                    return;
                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    display.setText("NO USERS FOUND :P");
                    display.setVisibility(View.VISIBLE);
                    retry.setVisibility(View.VISIBLE);
                }
            }
        }.execute(null , null , null);
    }

    private void findPair()  {
        InputStream inputStream = null;
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Common.SERVER_URL+CHAT_PAIR);
        JSONObject jsonObject = new JSONObject();

        String json = new String();

        try {
            jsonObject.put("user_id" , user_id);
            jsonObject.put("topic_id" , topic_id);
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
            pair_status_count++;
            Log.v("TopicChat" , "pair count = " + pair_status_count);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream = httpResponse.getEntity().getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StatusLine sl=httpResponse.getStatusLine();

        Log.v("TopicChat", Integer.toString(sl.getStatusCode()) + " " + sl.getReasonPhrase() + " FIND PAIR");

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
            pair_status = response.get("status").toString();
            Log.d("TopicChat" , pair_status + "  " + "IN") ;
            if(pair_status.equals("No users available") || pair_status.equals("No users available!Request expired.")){
                pair_status = response.get("status").toString() + " " + Integer.toString(pair_status_count);
                Log.d("TopicChat" , pair_status);
                Log.d("TopicChat" , Integer.toString(pair_status_count));
            }else{
                pair_status = response.get("status").toString();
                pair_status_count = 4;
                pair_id = (Integer)response.get("pair_id");
                chat = (Integer)response.get("chat");
                Log.d("USerFragment " , "pair_id : " + response.getString("pair_id") + " chat_id : " + response.get("chat"));
                SharedPreferences pref = getActivity().getSharedPreferences(Common.PREF, getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("chat" , chat);
                editor.commit();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void QuickBlocksChat(){
        Log.d("QuickBlocks" , "Entered QB chat after handler");
        QBDialog dialogToCreate = new QBDialog();
        dialogToCreate.setName("doesn't matter");
        //if(usersAdapter.getSelected().size() == 1){
        dialogToCreate.setType(QBDialogType.PRIVATE);
        //}else {
        //dialogToCreate.setType(QBDialogType.GROUP);
        //}
        //Log.v("UserId", " " + getUserIds(usersAdapter.getSelected()));
        //dialogToCreate.setOccupantsIds(getUserIds(usersAdapter.getSelected()));
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>();
        occupantIdsList.add(pair_id);
        dialogToCreate.setOccupantsIds(occupantIdsList);

        QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate, new QBEntityCallbackImpl<QBDialog>() {
            @Override
            public void onSuccess(QBDialog dialog, Bundle args) {
                //if(usersAdapter.getSelected().size() == 1){
                Log.v("Started chat", "started chat");
                startSingleChat(dialog);

            }
            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setMessage("dialog creation errors: " + errors).create().show();
            }
        });

            /*}
        });*/

        /*usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                loadNextPage();
                listViewIndex = usersList.getRefreshableView().getFirstVisiblePosition();
                View v = usersList.getRefreshableView().getChildAt(0);
                listViewTop = (v == null) ? 0 : v.getTop();
            }
        });

        loadNextPage();*/
    }

}
