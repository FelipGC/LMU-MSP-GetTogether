package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.Manifest;
import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Presenter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Spectator;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;

import java.lang.annotation.Target;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "preferences";
    private static final String PREF_USER = "username";
    private static final String PREF_SEEN = "seen";

    private ImageButton presenter;
    private ImageButton spectator;

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
    /**
     * The role of the user (Presenter/Spectator)
     */
    private static User userRole;

    private static ConnectionDataBase connectionDataBase;
    private static String userName = "UnknownUser";
    private boolean seen = false;

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
        presenter = (ImageButton)findViewById(R.id.buttonPresenter);
        spectator = (ImageButton) findViewById(R.id.buttonSpectator);
        //Setting up username via AlertDialog
        loadPreferences();
        if (!seen) {
            setUsername();
        }
        //Animations
        presenter.startAnimation(logoMoveAnimation);
        spectator.startAnimation(logoMoveAnimation);
        //Connection
        connectionDataBase = ConnectionDataBase.getInstance(); //Singleton
        connectionDataBase.setUpConnectionsClient(this);
        connectionDataBase.setServiceId(getPackageName());
    }

    @Override
    public void onPause() {
        super.onPause();
        savePreferences();

    }

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
    }

    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Edit and commit
        System.out.println("Save username: " + userName);
        editor.putString(PREF_USER, userName);
        editor.putBoolean(PREF_SEEN, true);
        editor.commit();
    }

    private void loadPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        // Get value
        userName = settings.getString(PREF_USER, userName);
        seen = settings.getBoolean(PREF_SEEN, seen);
        System.out.println("Load username: " + userName);
    }

    private void setUsername() {
        final EditText input = new EditText(this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //dialog.setTitle("Add a username");
        dialog.setMessage(R.string.username);
        dialog.setView(input);
        dialog.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Clicked enter", Toast.LENGTH_LONG).show();
                userName = input.getText().toString();
                savePreferences();
            }
        });
        dialog.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Clicked Skip", Toast.LENGTH_LONG).show();
                savePreferences();
            }
        });

        dialog.show();
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
        Log.i(TAG, "User chose to be a PRESENTER." + userName + seen);
        userRole = new Presenter(userName);
        startDiscovering();
    }

    /**
     * Gets executed if the user chooses to be a "Spectator"  by pressing
     * the corresponding button
     *
     * @param view
     */
    public void spectatorButtonClicked(View view) {
        Log.i(TAG, "User chose to be a SPECTATOR.");
        userRole = new Spectator(userName);
        startAdvertising();
    }

    /**
     * Calls startAdvertising() on the connectionDataBase
     */
    private void startAdvertising() {
        Toast.makeText(this, R.string.startAdvertising, Toast.LENGTH_LONG).show();
        connectionDataBase.startAdvertising();
        createSecondaryActivity();
    }

    /**
     * Calls stopAdvertising() on the connectionDataBase
     */
    private void stopAdvertising() {
        connectionDataBase.stopAdvertising();
    }
    /**
     * Calls startDiscovering() on the connectionDataBase
     */
    private void startDiscovering() {
        Toast.makeText(this, R.string.startDiscovering, Toast.LENGTH_LONG).show();
        connectionDataBase.startDiscovering();
        createSecondaryActivity();
    }
    /**
     * Calls stopDiscovering() on the connectionDataBase
     */
    private void stopDiscovering() {
        connectionDataBase.stopDiscovering();
    }

    /**
     * Creates a new (secondary) activity
     */
    private void createSecondaryActivity(){
        Intent intent = new Intent(this, SecondaryActivity.class);
        //intent.putExtra("RoleType", getUserRole().getRoleType());
        startActivity(intent);
    }
    //Getters and Setters
    public static User getUserRole() {
        return userRole;
    }

    public static void setUserRole(User userRole) {
        Log.i(TAG, "User changed his role to: " + userRole.getRoleType().toString());
        MainActivity.userRole = userRole;
    }
}
