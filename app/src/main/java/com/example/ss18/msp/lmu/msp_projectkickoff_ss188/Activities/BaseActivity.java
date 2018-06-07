package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class BaseActivity extends AppCompatActivity {

    protected final void onCreate(int layoutId) {
        setTheme(R.style.AppTheme);
        setContentView(layoutId);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
