package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "preferences_title";
    private static final String PREF_USER = "preferences_username";
    private EditText enteredUsername;
    private boolean userNameAlreadyEntered = false;
    private Button signUpButton;
    private TextView settingsText;

    /**
     * Tag for Logging/Debugging
     */
    private static final String TAG = "SETTINGS_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get the intent from MainActivity that someone selected the "Settings" option
        //on the activity menu
        Intent intent = getIntent();
        setContentView(R.layout.settings_fragment);
        signUpButton = (Button) findViewById(R.id.sign_up);
        settingsText = (TextView) findViewById(R.id.settings_text);

        enteredUsername = (EditText) findViewById(R.id.enter_username);
        loadPreferences();

        if (userNameAlreadyEntered) {
            signUpButton.setText("Save");
            settingsText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Gets called when the user selects "Change username" from the listed options
     */
    public void onClickChangeUsername(View button){
        Log.i(TAG,"Change username option clicked");
        setUsername();
    }

    /**
     * Gets called when the user selects "Change photo" from the listed options
     */
    public void onClickChangePhoto(View button){
        Log.i(TAG,"Change photo option clicked");
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

    /**
     * Loads the username form the preferences
     */
    private void loadPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        // Set username if already existing
        if(userNameAlreadyEntered = settings.contains(PREF_USER))
        {
            LocalDataBase.setUserName(settings.getString(PREF_USER,LocalDataBase.getUserName()));
            Log.i(TAG,"Load username: " + LocalDataBase.getUserName());
        }
    }

    /**
     * Saves the username, that the user picked
     */
    private void setUsername() {

        if (!enteredUsername.getText().toString().isEmpty()) {
            LocalDataBase.setUserName(enteredUsername.getText().toString());
            savePreferences();
            if (userNameAlreadyEntered) {
                Toast.makeText(SettingsActivity.this, R.string.changedInfo,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SettingsActivity.this,
                        getString(R.string.welcomeUser, LocalDataBase.getUserName()),
                        Toast.LENGTH_LONG).show();
            }
            Intent intent = new Intent(SettingsActivity.this,MainActivity.class);
            startActivity(intent);
            } else {
            Toast.makeText(SettingsActivity.this, "You didn't enter any new nickname!", Toast.LENGTH_LONG).show();
        }
    }

}
