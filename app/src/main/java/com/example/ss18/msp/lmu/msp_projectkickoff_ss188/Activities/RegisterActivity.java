package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class RegisterActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "preferences_title";
    private static final String PREF_USER = "preferences_username";

    private AlertDialog alertDialog;
    /**
     * Tag for Logging/Debugging
     */
    private static final String TAG = "REGISTER_ACTIVITY";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUsername();
    }

    /**
     * Displays an input dialog window to enter the username
     */
    private void setUsername() {
        final EditText input = new EditText(this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.welcome);
        dialog.setMessage(R.string.username);
        dialog.setView(input);
        dialog.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalDataBase.setUserName(input.getText().toString());
                savePreferences();
                Toast.makeText(RegisterActivity.this, "Welcome " +
                        LocalDataBase.getUserName()+ "!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        dialog.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(RegisterActivity.this, "Skipped entering a username", Toast.LENGTH_LONG).show();
                savePreferences();
            }
        });

        alertDialog = dialog.create();
        alertDialog.show();
    }
    /**
     * Saves the username to the preferences
     */
    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Edit and commit
        Log.i(TAG,"Save username: " + LocalDataBase.getUserName());
        editor.putString(PREF_USER, LocalDataBase.getUserName());
        editor.commit();
    }
}
