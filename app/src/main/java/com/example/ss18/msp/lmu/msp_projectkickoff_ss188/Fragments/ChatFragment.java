package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat.Message;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat.MessageAdapter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;

import java.io.File;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "InboxFragment";

    private EditText editText;
    Message mes;
    String us;
    MessageAdapter messageAdapter = new MessageAdapter(this.getContext());
    ListView messagesView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment,container,false);
        editText = (EditText) view.findViewById(R.id.editText);
        messagesView = (ListView) view.findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
        us = "Koko";
        mes = new Message("Sup!", us, true);
        messageAdapter.add(mes);
        return view;
    }

    public void sendMessage(View view) {
        String message = editText.getText().toString();
        if (message.length() > 0) {
            String user = "Eli";
            Message msg = new Message(message, user, false);


            Log.i("Main", "Hereee" + messageAdapter+msg+mes);
            messageAdapter.add(msg);
            messageAdapter.add(mes);
            // scroll the ListView to the last added element
            messagesView.setSelection(messagesView.getCount() - 1);

            editText.getText().clear();
        }
    }
}
