package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

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
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.AbstractConnectionService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.ChatMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.OldConnection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.OldConnection.PayloadSender;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ChatFragment";

    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    private ImageButton buttonSend;
    private PayloadSender payloadSender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat,container,false);
        editText = (EditText) view.findViewById(R.id.editText);
        messagesView = (ListView) view.findViewById(R.id.messages_view);
        if(messageAdapter==null){
            messageAdapter = new MessageAdapter(getActivity());
            for(BaseMessage msg : ((AppLogicActivity)getActivity()).getDistributionService().getMessages()){
                if(msg.getClass()==ChatMessage.class){
                    addReceivedMessage((ChatMessage) msg);
                }
            }
        }
        buttonSend = (ImageButton) view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
        messagesView.setAdapter(messageAdapter);
        payloadSender = ConnectionManager.getInstance().getPayloadSender();

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

            Message msg = new Message(messageText,null, name, true);
            messageAdapter.addMessage(msg);
            // scroll the ListView to the last added element
            messagesView.setSelection(messagesView.getCount() - 1);

            editText.getText().clear();
            sendMessage(messageText);
        }
    }

    /**
     * Sends the message to (all) endpoints
     * @param message is a string
     */
    private void sendMessage(String message) {
        AbstractConnectionService service = ((AppLogicActivity)getActivity()).getConnectionService();
        service.broadcastChatMessage(message);
    }

    /*
    ** Gets the message from the endpoint
     */
    public void addReceivedMessage(ChatMessage message) {

        //Extracts the payloadSender and the message from the message and converts it into
        //Message(). The format is sender:filename.
        Log.i(TAG, "Message is full: " + message);

        if(messageAdapter==null){
            return;
        }
        Message received = new Message(message.getBody(), message.getId(), message.getSender(), false);
        messageAdapter.addMessage(received);
        // scroll the ListView to the last added element
        messagesView.setSelection(messagesView.getCount() - 1);
    }
    /**
     * Displays a neutral system chat message in the chat
     */
    public void displaySystemNotification(String message) {
        if(messageAdapter == null)
            return;

        Message received = new Message(message, null, "SYSTEM", false);
        messageAdapter.addMessage(received);
        // scroll the ListView to the last added element
        messagesView.setSelection(messagesView.getCount() - 1);
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
