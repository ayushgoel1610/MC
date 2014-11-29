package com.iiitd.mcproject.Chat.ui;

import android.os.Bundle;
import android.util.Log;

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

    @Override
    public void processMessage(QBPrivateChat chat, final QBChatMessage message) {
        Log.w(TAG, "new incoming message: " + message);

        chatActivity.runOnUiThread(new Runnable() {
                                   public void run() {
                                       for (QBAttachment attachment : message.getAttachments()) {
                                           Integer fileId = Integer.parseInt(attachment.getId());

                                           // download a file
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
                                                   } catch (Exception e) {

                                                   }
                                                   Log.v("PhotoPath", destinationFile.getAbsolutePath());
                                                   message.setProperty("URI", destinationFile.getAbsolutePath());


                                               }

                                               @Override
                                               public void onError(List<String> errors) {
                                                   // errors
                                               }
                                           });
                                       }
                                   }
                               });
        chatActivity.showMessage(message);
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
