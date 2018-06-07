package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.InboxAdapter;

import java.io.File;

public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";
    private InboxAdapter inboxAdapter;
    private final int READ_REQUEST_CODE = 4242;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        ListView inboxListView = view.findViewById(R.id.inboxListView);
        inboxListView.setAdapter(inboxAdapter = new InboxAdapter(getActivity()));
        //Set ClickListener (normal click)
        inboxListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ConnectionEndpoint endpoint = (ConnectionEndpoint) view.getTag();
                Log.i(TAG, "Clicked on presenter: " + endpoint.getName());
                performFileSearch(pathToEndpoint(endpoint));
            }
        });
        return view;
    }

    /**
     *Renames and saves the payload to a specific location
     */
    public void storePayLoad(ConnectionEndpoint endpoint, String fileName, File file) {

        Log.i(TAG, "Received and renamed payload file to: " + file.getName());
        inboxAdapter.add(endpoint);
        //Create folder to save the date if it does not exist already.
        File f = new File(Environment.getExternalStorageDirectory() + "/" + "GetTogether", endpoint.getName());
        if (!f.exists()) {
            f.mkdirs();
        }
        //Rename file
        file.renameTo(new File(pathToEndpoint(endpoint), fileName));
    }

    /**
     * Returns the path to the location where the files from a particular endpoint should be stored
     */
    private String pathToEndpoint(ConnectionEndpoint endpoint){
        return Environment.getExternalStorageDirectory() + "/" + "GetTogether" + "/" + endpoint.getName();
    }
    /**
     * Fires an intent to spin up the "file chooser" UI and select a file.
     */
    private void performFileSearch(String path) {
        Log.i(TAG,"performFileSearch() PATH= " + path);
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter types (*/* == everything)
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent,path), READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Log.i(TAG, "onActivityResult()");
            }
        }
    }
}
