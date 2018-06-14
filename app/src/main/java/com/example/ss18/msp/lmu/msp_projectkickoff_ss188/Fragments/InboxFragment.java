package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;


public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";
    private final int READ_REQUEST_CODE = 4242;
    private TextView title;
    private int fileCounter = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        ImageButton button = view.findViewById(R.id.inboxFilePicker);
        title = view.findViewById(R.id.inboxTitle);
        //Set ClickListener (normal click)
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked on file picker.");
                performFileSearch();
            }
        });
        return view;
    }

    /**
     *Renames and saves the payload to a specific location
     */
    public void updateInboxFragment() {
        Log.i(TAG, "updateInboxFragment()");
        title.setText("<Files: " + ++fileCounter + ">");
    }

    /**
     * Fires an intent to spin up the "file chooser" UI and select a file.
     */
    private void performFileSearch() {
        Log.i(TAG,"performFileSearch()");
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter types (*/* == everything)
        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Log.i(TAG, "onActivityResult(): " + resultData);
            }
        }
    }
}
