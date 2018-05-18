package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class SecondaryActivity extends AppCompatActivity {
    /**
     * Tag for Logging/Debugging
     */
    private static final String TAG = "SECONDARY_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);
        Log.i(TAG,"Secondary activity created");
    }
}
