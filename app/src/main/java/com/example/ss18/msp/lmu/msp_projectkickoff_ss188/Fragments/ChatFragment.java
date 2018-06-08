package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.arch.lifecycle.LifecycleOwner;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat.Message;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat.MessageAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.google.android.gms.nearby.connection.Payload;

import java.nio.charset.Charset;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ChatFragment";

    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    private ImageButton buttonSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment,container,false);
        editText = (EditText) view.findViewById(R.id.editText);
        messagesView = (ListView) view.findViewById(R.id.messages_view);
        messageAdapter = new MessageAdapter(getActivity());
        buttonSend = (ImageButton) view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);

        messagesView.setAdapter(messageAdapter);

        return view;
    }

    /*
     ** Gets executed when the user presses the "Send" button
     */
    @Override
    public void onClick(View button) {
        Log.i(TAG, "Clicked Send");
        String messageText = editText.getText().toString();
        if (!messageText.isEmpty()) {
            
            String name = LocalDataBase.getUserName();
            Bitmap profilePicture = LocalDataBase.getProfilePicture();
            Message msg = new Message(messageText, name, profilePicture, true);
            messageAdapter.addMessage(msg);
            // scroll the ListView to the last added element
            messagesView.setSelection(messagesView.getCount() - 1);

            editText.getText().clear();
            sendDataToEndpoints(messageText);

        }
    }

    /**
     * Sends the message to (all) endpoints
     * @param message is a string
     */
    private void sendDataToEndpoints(String message) {
        //String name = LocalDataBase.getUserName();
        Payload payload = dataToPayload(message);
        // Adding the CHAT tag to identify chat messages on receive.
        String payloadStoringName = "CHAT" + ":" + LocalDataBase.getUserName() + ":" + message;
        Log.i(TAG, "SendDataToEndpoint: " + payloadStoringName);
        //Send message

        AppLogicActivity.getConnectionManager().sendPayload(payload,payloadStoringName);
    }

    /**
     * Transforms string into a payload so we can send messages between
     * different devices
     */
    private Payload dataToPayload(String message) {
        // Create Bytes from String
        Payload payload = Payload.fromBytes(message.getBytes(Charset.forName("UTF-8")));
        Log.i(TAG, "Data to payload:  " + payload.getType());
        return payload;
    }

    /*
    ** Gets the message from the endpoint
     */
    public void getDataFromEndPoint(String receivedMessage, Bitmap profilePicture) {

        //Extracts the payloadSender and the message from the message and converts it into
        //Message(). The format is sender:filename.
        Log.i(TAG, "Message is full: " + receivedMessage + "   " + profilePicture);
        int substringDividerIndex = receivedMessage.indexOf(':');
        String payloadSender = receivedMessage.substring(0, substringDividerIndex);
        String message = receivedMessage.substring(substringDividerIndex + 1);
        Message received = new Message(message, payloadSender, profilePicture, false);
        messageAdapter.addMessage(received);
        // scroll the ListView to the last added element
        messagesView.setSelection(messagesView.getCount() - 1);
    }
}
