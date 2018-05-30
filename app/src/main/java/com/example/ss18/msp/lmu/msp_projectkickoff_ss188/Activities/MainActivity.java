package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Presenter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Spectator;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "preferences_title";
    private static final String PREF_USER = "preferences_username";

    private ImageButton presenter;
    private ImageButton spectator;
    private AlertDialog alertDialog;

    /**
     * ACCESS_COARSE_LOCATION is considered dangerous, so we need to explicitly
     * grant the permission every time we start the app
     */
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
            };

    /**
     * Tag for Logging/Debugging
     */
    private static final String TAG = "MAIN_ACTIVITY";

    private boolean userNameAlreadyEntered = false;

    /**
     * Called when our Activity has been made visible to the user.
     * This is only needed for newer devices
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        //Check if we have all permissions, if not, then add!
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(REQUIRED_PERMISSIONS, 1);
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Animation logoMoveAnimation = AnimationUtils.loadAnimation(this, R.anim.pop_up_animation);
        //Views
        presenter = findViewById(R.id.buttonPresenter);
        spectator = findViewById(R.id.buttonSpectator);
        //Setting up username via AlertDialog
        loadPreferences();
        if (!userNameAlreadyEntered) {
            setUsername();
        } else {
            Toast.makeText(MainActivity.this, "HI! " + LocalDataBase.getUserName(), Toast.LENGTH_LONG).show();
        }

        //Animations
        presenter.startAnimation(logoMoveAnimation);
        spectator.startAnimation(logoMoveAnimation);
    }

    /**
     * Adds the menu button to the view
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Gets executed when the user selects "Settings" on the activity menu
     */
    public void onClickSettings(MenuItem item){
        Log.i(TAG,"Settings option clicked");
        Intent myIntent = new Intent(MainActivity.this,SettingsActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    /**
     * Gets executed when the user selects "About" on the activity menu
     */
    public void onClickAbout(MenuItem item){

        Log.i(TAG,"About option clicked");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("About");
        dialog.setMessage(R.string.aboutTextCredits);
        dialog.setNeutralButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    /**
     * Gets executed when the user selects "Help" on the activity menu
     */
    public void onClickHelp(MenuItem item){
        Log.i(TAG,"Help option clicked");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Help & Feedback");
        dialog.setMessage(R.string.help_feedback);
        dialog.setNeutralButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(alertDialog != null) alertDialog.dismiss();
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
                Toast.makeText(MainActivity.this, "Welcome " +
                        LocalDataBase.getUserName()+ "!", Toast.LENGTH_LONG).show();
            }
        });
        dialog.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Skipped entering a username", Toast.LENGTH_LONG).show();
                savePreferences();
            }
        });

        alertDialog = dialog.create();
        alertDialog.show();
    }

    /**
     * Called when the user has accepted (or denied) our permission request.
     */
    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, R.string.missingPermission, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Permission error");
                    finish();
                    return;
                }
            }
            recreate();
        }
    }

    /**
     * Gets executed if the user chooses to be a "Presenter"  by pressing
     * the corresponding button
     *
     * @param view
     */
    public void presenterButtonClicked(View view) {
        Log.i(TAG, "User chose to be a PRESENTER." + LocalDataBase.getUserName());
        createSecondaryActivity(new Presenter());
    }

    /**
     * Gets executed if the user chooses to be a "Spectator"  by pressing
     * the corresponding button
     *
     * @param view
     */
    public void spectatorButtonClicked(View view) {
        Log.i(TAG, "User chose to be a SPECTATOR.");
        createSecondaryActivity(new Spectator());
    }

    /**
     * Creates a new (secondary) activity
     */
    private void createSecondaryActivity(User userRole){
        Intent intent = new Intent(this, AppLogicActivity.class);
        intent.putExtra("UserRole", userRole);
        startActivity(intent);
    }
}
