package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public abstract class BaseActivity extends AppCompatActivity {

    private final static String TAG = "BaseActivity";
    protected final void onCreate(int layoutId) {
        setTheme(R.style.AppTheme);
        setContentView(layoutId);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    @Override
    public boolean onSupportNavigateUp() {
        terminateService();
        onBackPressed();
        return true;
    }

    private void terminateService(){
        Log.i(TAG, "onDestroy() -> stopService()");
        Intent intent = new Intent(getApplicationContext(), ConnectionManager.class);
        stopService(intent);
    }
    @Override
    protected void onDestroy() {
        terminateService();
        super.onDestroy();
    }
}
