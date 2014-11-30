package com.iiitd.mcproject.Chat.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.iiitd.mcproject.Chat.ui.activities.ChatActivity;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListenerImpl;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.QBContent;
import com.quickblox.core.QBEntityCallbackImpl;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class PrivateChatManagerImpl extends QBMessageListenerImpl<QBPrivateChat> implements ChatManager, QBPrivateChatManagerListener {

    private static final String TAG = "PrivateChatManagerImpl";

    private ChatActivity chatActivity;
    private String photo_uri;
    private QBPrivateChatManager privateChatManager;
    private QBPrivateChat privateChat;

    public PrivateChatManagerImpl(ChatActivity chatActivity, Integer opponentID) {
        this.chatActivity = chatActivity;
        //Handler handler = new Handler():
        privateChatManager = QBChatService.getInstance().getPrivateChatManager();

        privateChatManager.addPrivateChatManagerListener(this);

        // init private chat
        //
        privateChat = privateChatManager.getChat(opponentID);
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(opponentID, this);
        }else{
            privateChat.addMessageListener(this);
        }
    }

    @Override
    public void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException {
        privateChat.sendMessage(message);
    }

    @Override
    public void release() {
        Log.w(TAG, "release private chat");
        privateChat.removeMessageListener(this);
        privateChatManager.removePrivateChatManagerListener(this);
    }

    QBChatMessage receivedmessage;
    @Override
    public void processMessage(QBPrivateChat chat, final QBChatMessage message) {
        Log.w(TAG, "new incoming message: " + message);

        chatActivity.runOnUiThread(new Runnable() {
                                   public void run() {
                                       for (QBAttachment attachment : message.getAttachments()) {
                                           Integer fileId = Integer.parseInt(attachment.getId());

                                           // download a file
                                           Toast.makeText(chatActivity, "Receiving image from friend..", Toast.LENGTH_SHORT).show();
                                           QBContent.downloadFileTask(fileId, new QBEntityCallbackImpl<InputStream>() {
                                               @Override
                                               public void onSuccess(InputStream inputStream, Bundle params) {
                                                   // process file
                                                   File destinationFile = new File("/sdcard/" + "image.png");
                                                   BufferedOutputStream buffer;
                                                   try {
                                                       buffer = new BufferedOutputStream(new FileOutputStream(destinationFile));
                                                       byte byt[] = new byte[1024];
                                                       int i;
                                                       for (long l = 0L; (i = inputStream.read(byt)) != -1; l += i) {
                                                           buffer.write(byt, 0, i);
                                                       }
                                                       buffer.close();
                                                       Log.v("Before calling getURI", "");
                                                       photo_uri = destinationFile.getAbsolutePath();
                                                       QBChatMessage returnmessage = new QBChatMessage();
                                                        returnmessage = message;
                                                       Log.v("PhotoPath", photo_uri);
                                                       //returnmessage.removeProperty("uri");
                                                       returnmessage.setBody(photo_uri);
                                                       returnmessage.setProperty("uri", "sample property");
                                                       Log.v("The message",  returnmessage + "");
                                                       Log.v("Property saved", returnmessage.getBody());
                                                       //chatActivity.showMessage(returnmessage);
                                                       chatActivity.adapter.notifyDataSetChanged();
                                                       return;
                                                   } catch (Exception e) {
                                                        Log.v("Error in photo", e + "");
                                                   }




                                               }

                                               @Override
                                               public void onError(List<String> errors) {
                                                   // errors
                                                   Log.v("Error uploading photo", errors + "");
                                               }
                                           });
                                       }
                                   }
                               });
        Log.v("SHow", "show message already getting called");
        chatActivity.showMessage(message);
    }

    public String getURI(File file, String path){
        Log.v("Entered GETuri","");
        /*String filePath = file.getAbsolutePath();
        Cursor cursor = chatActivity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            int id_photo = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            photo_uri =  Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id_photo).toString();
        } else {
            if (file.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                photo_uri =  chatActivity.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values).toString();
            }
        }*/
        Uri selectedImage = Uri.parse(path);
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = chatActivity.getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        photo_uri = cursor.getString(columnIndex);
        cursor.close();
        Log.v("Uri returned from function", "Entered the function" + photo_uri);
        return  photo_uri;
    }
    @Override
    public void processError(QBPrivateChat chat, QBChatException error, QBChatMessage originChatMessage){

    }

    @Override
    public void chatCreated(QBPrivateChat incomingPrivateChat, boolean createdLocally) {
        if(!createdLocally){
            privateChat = incomingPrivateChat;
            privateChat.addMessageListener(PrivateChatManagerImpl.this);
        }

        Log.w(TAG, "private chat created: " + incomingPrivateChat.getParticipant() + ", createdLocally:" + createdLocally);
    }
}
