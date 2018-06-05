package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.InboxActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Inbox.InboxAdapterItem;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Inbox.InboxAdapter;

import java.io.File;

public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";
    private static final int READ_REQUEST_CODE = 123;
    private InboxAdapter inboxAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inbox_fragment,container,false);
        ListView inboxListView = view.findViewById(R.id.inboxListView);
        inboxListView.setAdapter(inboxAdapter = new InboxAdapter(getActivity()));
        //Set ClickListener (normal click)
        inboxListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InboxAdapterItem item = (InboxAdapterItem) view.getTag();
                Log.i(TAG,"Clicked on presenter: " + item.getConnectionEndpoint().getName());
                Intent intent = new Intent(getActivity(), InboxActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
    public void storePayLoad(ConnectionEndpoint endpoint, String fileName, File payloadFile) {
        //Rename file
        payloadFile.renameTo(new File(payloadFile.getParentFile(), fileName));
        Log.i(TAG,"Received and renamed payload file to: " + payloadFile.getName());
        inboxAdapter.add(endpoint,payloadFile);
        //Set ClickListener
    }
}
