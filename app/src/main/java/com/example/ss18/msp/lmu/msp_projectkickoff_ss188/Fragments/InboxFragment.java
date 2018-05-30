package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.io.File;

public class InboxFragment extends Fragment {
    private static final String TAG = "InboxFragment";
    private static final int READ_REQUEST_CODE = 123;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.inbox_fragment,container,false);
        return view;
    }
    public void storePayLoad(String fileName, File payloadFile) {
        //Rename file
        payloadFile.renameTo(new File(payloadFile.getParentFile(), fileName));
        Log.i(TAG,"Renamed payload file to: " + payloadFile.getName());
    }
}
