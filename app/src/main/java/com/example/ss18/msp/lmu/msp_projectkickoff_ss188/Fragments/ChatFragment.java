package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
import com.google.android.gms.nearby.connection.Payload;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ChatFragment";

    private EditText editText;
    private Message mes;
    private String us;
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
        Log.i("Main", "Hereee" + getActivity());

        us = "Koko";
        mes = new Message("Sup!", us, false);
        messageAdapter.add(mes);
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
            Message msg = new Message(messageText, name, true);
            messageAdapter.add(msg);
            messageAdapter.add(mes);
            // scroll the ListView to the last added element
            messagesView.setSelection(messagesView.getCount() - 1);

            sendDataToEndpoint(messageText);

            editText.getText().clear();
        }
    }

    /**
     * Sends the message to (all) endpoints
     * @param message is a string
     */
    private void sendDataToEndpoint(String message) {
        //String name = LocalDataBase.getUserName();
        Payload payload = dataToPayload(message);
        // Mapping the ID of the file payload to the filename
        String payloadStoringName = payload.getId() + ":" + message;
        Log.i(TAG, "Eliiii2222  " + payloadStoringName);

        AppLogicActivity.getConnectionManager().sendPayload(payload,payloadStoringName);
    }

    /**
     * Transforms string into a payload so we can send messages between
     * different devices
     */
    private Payload dataToPayload(String message) {
        // Create Bytes from String
        Payload payload = Payload.fromBytes(message.getBytes(Charset.forName("UTF-8")));
        Log.i(TAG, "Eliiii  " + payload.getType());
        return payload;
    }

    /*
    ** Gets the message from the endpoint
     */
    private void getDataFromEndPoint(Payload receivedPayload) {
        String receivedMessage;
        try {
            receivedMessage = new String(receivedPayload.asBytes(), "UTF-8");
            //Extracts the payloadSender and the message from the message and converts it into
            //Message(). The format is sender:filename.
            int substringDividerIndex = receivedMessage.indexOf(':');
            String payloadSender = receivedMessage.substring(0, substringDividerIndex);
            String message = receivedMessage.substring(substringDividerIndex + 1);

            Message received = new Message(message, payloadSender, false);
            messageAdapter.add(received);
            // scroll the ListView to the last added element
            messagesView.setSelection(messagesView.getCount() - 1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
        }
    }
}