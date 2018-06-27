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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;


import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat.Message;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat.MessageAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.PayloadSender;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ChatFragment";

    private EditText editText;
    private static MessageAdapter messageAdapter;
    private static ListView messagesView;
    private ImageButton buttonSend;
    private PayloadSender payloadSender;
    private ArrayList<Message> messages = new ArrayList<>();

   public void setAdapter(){
       Log.i(TAG,"SetAdapter()");
   }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG,"onCreateView()");
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        editText = (EditText) view.findViewById(R.id.editText);
        messageAdapter = new MessageAdapter(getContext());
        if(messagesView == null) {
            for (Message msg : messages) {
                messageAdapter.addMessage(msg);
            }
        }
        messagesView = (ListView) view.findViewById(R.id.messages_view);
        buttonSend = (ImageButton) view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
        messagesView.setAdapter(messageAdapter);
        payloadSender = new PayloadSender();
        return view;
    }

    @Override
    public void onPause(){
        Log.i("TAG", "Chat Fragment onPause");
        super.onPause();

        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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

            Message msg = new Message(messageText,null, name, true);
            messageAdapter.addMessage(msg);
            // scroll the ListView to the last added element
            if(messagesView != null) {
                messageAdapter.notifyDataSetChanged();
                messagesView.setSelection(messagesView.getCount() - 1);
            }

            editText.getText().clear();
            sendDataToEndpoints(messageText);

        }
    }

    /**
     * Sends the message to (all) endpoints
     * @param message is a string
     */
    private void sendDataToEndpoints(String message) {
        try {
            payloadSender.sendChatMessage(message);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*
    ** Gets the message from the endpoint
     */
    public void getDataFromEndPoint(String id, String receivedMessage) {

        //Extracts the payloadSender and the message from the message and converts it into
        //Message(). The format is sender:filename.
        Log.i(TAG, "Message is full: " + receivedMessage);
        int substringDividerIndex = receivedMessage.indexOf(':');
        String payloadSender = receivedMessage.substring(0, substringDividerIndex);
        String message = receivedMessage.substring(substringDividerIndex + 1);

        Message received = new Message(message, id, payloadSender, false);
        messages.add(received);
        // scroll the ListView to the last added element
        if(messagesView != null) {
            messageAdapter.addMessage(received);
            messageAdapter.notifyDataSetChanged();
            messagesView.setSelection(messagesView.getCount() - 1);
        }
    }
    /**
     * Displays a neutral system chat message in the chat
     */
    public void displaySystemNotification(String message) {
        Message received = new Message(message, null, "SYSTEM", false);
        messages.add(received);
        // scroll the ListView to the last added element
        if(messagesView != null) {
            messageAdapter.addMessage(received);
            messageAdapter.notifyDataSetChanged();
            messagesView.setSelection(messagesView.getCount() - 1);
        }
    }

    /*
     ** Clears all messages, if the connection was destroyed
     */
    public void clearContent() {
        Log.i(TAG, "Clear the chat content.");
        if(messageAdapter != null)
            messageAdapter.clearContent();
    }
}
