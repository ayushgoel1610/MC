package com.iiitd.mcproject.Chat.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iiitd.mcproject.Chat.ui.ChatManager;
import com.iiitd.mcproject.Chat.ui.PrivateChatManagerImpl;
import com.iiitd.mcproject.Chat.ui.adapters.ChatAdapter;
import com.iiitd.mcproject.Common;
import com.iiitd.mcproject.R;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatHistoryMessage;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBMessage;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBCustomObjectRequestBuilder;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final String END_CHAT="endchat";

    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_DIALOG = "dialog";
    public boolean doubleBackToExitPressedOnce = false;
    private final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    private static int RESULT_LOAD_IMAGE = 1;

    private EditText messageEditText;
    private ListView messagesContainer;
    private Button sendButton;
    private Button photoButton;
    private Button addB;
    private Button subB;
    private TextView countB;
    private int counter;
    private ProgressBar progressBar;
    private static ProgressBar pb;

    private Mode mode = Mode.PRIVATE;
    private ChatManager chat;
    private ChatAdapter adapter;
    private QBDialog dialog;
    private static int pair_id;
    private static View messageview;
    private ArrayList<QBChatHistoryMessage> history;

    public static void start(Context context, Bundle bundle) {
        NewDialogActivity.exit_flag = 1;
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtras(bundle);
        pair_id = intent.getIntExtra("pair_id", -1);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        messageview = vi.inflate(R.layout.list_item_message, null);

        pb = (ProgressBar) messageview.findViewById(R.id.progressBar);
        Toast.makeText(getApplicationContext(), "Press back to exit the chat",
                Toast.LENGTH_LONG).show();

        initViews();
        counter = 0;
        countB.setText("" + counter);
        addB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter++;
                countB.setText("" + counter);
            }
        });

        subB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                counter--;
                if (counter < 0)
                    counter = 0;
                countB.setText("" + counter);
            }
        });

    }

    @Override
    public void onBackPressed() {
        /*if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit chat", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);*/

        SharedPreferences pref = getSharedPreferences(Common.PREF, MODE_PRIVATE);
        int chat = pref.getInt("chat", 0);
        Log.d("ChatActivity" , "The chat id is : "  + chat);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("chat");
        editor.commit();

        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        alertbox.setTitle("Quit Chat");
        alertbox.setMessage("The User reputation is : " + counter + "\nDo you want to exit the chat?");
        alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {


            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(getApplicationContext(), "'yes' button clicked", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {


            public void onClick(DialogInterface arg0, int arg1) {
                //Toast.makeText(getApplicationContext(), "'No' button clicked", Toast.LENGTH_SHORT).show();
            }
        });

        alertbox.show();
    }

    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        addB = (Button) findViewById(R.id.addB);
        subB = (Button) findViewById(R.id.subB);
        countB = (TextView) findViewById(R.id.np);
        sendButton = (Button) findViewById(R.id.chatSendButton);
        photoButton = (Button) findViewById(R.id.photoSendButton);
        TextView meLabel = (TextView) findViewById(R.id.meLabel);
        meLabel.setText("You");
        TextView companionLabel = (TextView) findViewById(R.id.companionLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();

        // Get chat dialog
        //
        dialog = (QBDialog)intent.getSerializableExtra(EXTRA_DIALOG);

        mode = (Mode) intent.getSerializableExtra(EXTRA_MODE);
        switch (mode) {
            case PRIVATE:
                //Integer opponentID = ((ApplicationSingleton)getApplication()).getOpponentIDForPrivateDialog(dialog);

                chat = new PrivateChatManagerImpl(this, pair_id);

                companionLabel.setText("Friend");

                // Load CHat history
                //
                //loadChatHistory();
                adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBMessage>());
                messagesContainer.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
                break;
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                // Send chat message
                //
                QBChatMessage chatMessage = new QBChatMessage();
                chatMessage.setBody(messageText);
                chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
                chatMessage.removeProperty("URI");

                try {
                    chat.sendMessage(chatMessage);
                } catch (XMPPException e) {
                    Log.e(TAG, "failed to send a message", e);
                } catch (SmackException sme){
                    Log.e(TAG, "failed to send a message", sme);
                }

                messageEditText.setText("");
                pb.setVisibility(View.GONE);
                if(mode == Mode.PRIVATE) {
                    showMessage(chatMessage);
                }
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String picturePath = cursor.getString(columnIndex);
            cursor.close();
            //Log.v("path", picturePath);
            //Log.v("URI", "" + selectedImage);
            //int fileId2 = R.raw.firewalloff;
            //InputStream is2 = getResources().openRawResource(fileId2);
            //File file2 = FileHelper.getFileInputStream(is2, "10611988_10202173825058096_703047747_o.jpg", "Download");
            File file = new File(picturePath);
            //Log.v("file", "" + file);
            Boolean fileIsPublic = false;
            QBContent.uploadFileTask(file, fileIsPublic, null, new QBEntityCallbackImpl<QBFile>() {
                @Override
                public void onSuccess(QBFile file, Bundle params) {

                    // create a message
                    QBChatMessage chatMessage = new QBChatMessage();
                    chatMessage.setProperty("save_to_history", "1"); // Save a message to history

                    // attach a photo
                    QBAttachment attachment = new QBAttachment("photo");
                    attachment.setId(file.getId().toString());
                    chatMessage.addAttachment(attachment);
                    //chatMessage.setBody("::Photo Sent::");
                    chatMessage.setProperty("uri",picturePath);

                    // send a message
                    try {
                        chat.sendMessage(chatMessage);
                    } catch (XMPPException e) {
                        Log.e(TAG, "failed to send a message", e);
                    } catch (SmackException sme) {
                        Log.e(TAG, "failed to send a message", sme);
                    }

                    pb.setVisibility(View.VISIBLE);
                    if(mode == Mode.PRIVATE) {
                        showMessage(chatMessage);
                    }

                }

                @Override
                public void onError(List<String> errors) {
                    // error
                    Log.v("Error on upload", "" +  errors);
                }
            });

            //ImageView imageView = (ImageView) findViewById(R.id.imgView);
            //imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }


    }

                /*int fileId = R.raw.firewalloff;
                InputStream is = getResources().openRawResource(fileId);
                File file = FileHelper.getFileInputStream(is, "sample_photo.png", "myFile");
                Boolean fileIsPublic = false;
                QBContent.uploadFileTask(file, fileIsPublic, null, new QBEntityCallbackImpl<QBFile>() {
                    @Override
                    public void onSuccess(QBFile file, Bundle params) {

                        // create a message
                        QBChatMessage chatMessage = new QBChatMessage();
                        chatMessage.setProperty("save_to_history", "1"); // Save a message to history

                        // attach a photo
                        QBAttachment attachment = new QBAttachment("photo");
                        attachment.setId(file.getId().toString());
                        chatMessage.addAttachment(attachment);

                        // send a message
                        try {
                            chat.sendMessage(chatMessage);
                        } catch (XMPPException e) {
                            Log.e(TAG, "failed to send a message", e);
                        } catch (SmackException sme) {
                            Log.e(TAG, "failed to send a message", sme);
                        }
                    }

                    @Override
                    public void onError(List<String> errors) {
                        // error
                        Log.v("Error on upload", "Error");
                    }
                });
            }
        });
                Log.v("File uploaded","ok");

*/


    private void loadChatHistory(){
        QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        customObjectRequestBuilder.setPagesLimit(100);

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatHistoryMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatHistoryMessage> messages, Bundle args) {
                history = messages;

                adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBMessage>());
                messagesContainer.setAdapter(adapter);

                for (QBMessage msg : messages) {
                    showMessage(msg);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                dialog.setMessage("load chat history errors: " + errors).create().show();
            }
        });
    }

    public void showMessage(QBMessage message) {
        adapter.add(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    public static enum Mode {PRIVATE, GROUP}

    private class EndChat extends AsyncTask<Void , Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
    }

}
