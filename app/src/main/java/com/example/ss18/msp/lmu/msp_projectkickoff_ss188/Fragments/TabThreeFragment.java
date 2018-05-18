package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class TabThreeFragment extends Fragment {
    private static final String TAG = "TabThreeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_three_fragment,container,false);
        return view;
    }
}
