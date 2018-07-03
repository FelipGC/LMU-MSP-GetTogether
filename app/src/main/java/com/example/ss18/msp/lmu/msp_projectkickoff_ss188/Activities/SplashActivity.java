package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.AppPreferences;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SPLASH_ACTIVITY";

    // region Check permissions on App-Start
    /**
     * ACCESS_COARSE_LOCATION is considered dangerous, so we need to explicitly
     * grant the permission every time we start the app
     */
    private static final String[] REQUIRED_PERMISSIONS =
            new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.VIBRATE,
                    Manifest.permission.RECORD_AUDIO
            };

    /**
     * Called when our Activity has been made visible to the user.
     * This is only needed for newer devices
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setRequiredPermissions() {
        Log.i(TAG, "AsetRequiredPermissions()");
        //Check if we have all permissions, if not, then add!
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Requesting permission: " + permission);
                requestPermissions(REQUIRED_PERMISSIONS, 1);
            }
        }
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
    //endregion

    //region Setting up Notifications
    private final String CHANNEL_ID = "CHANNEL_ID_42";
    private final String PROGRESS_ID = "CHANNEL_ID_12";
    /**
     * Called when the Activity starts to create a notification channel
     * This is only needed for newer devices
     */
    @TargetApi(26)
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationChannel progreeChannel = new NotificationChannel(PROGRESS_ID, name, importance);
            progreeChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(progreeChannel);
        }
    }
    //endregion

    private AppPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        //Disable rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setTheme(R.style.AppTheme);
        preferences = AppPreferences.getInstance(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setRequiredPermissions();
        }else{
            onAllPermissionsGranted();
        }
        createNotificationChannel();

        //Animate text hint
        TextView textView = findViewById(R.id.touchToStart);
        textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_out));
        //Animate Image
        ImageView imageView = findViewById(R.id.user_image);
        imageView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.pop_up_animation));

    }

    private void onAllPermissionsGranted(){
        String username = preferences.getUsername();
        Log.i(TAG, "userNameAlreadyEntered: " + username);
        if (username == null) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("newUser", true);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            LocalDataBase.setUserName(username); //TODO Laureen! Das hier nicht vergessen sonst bugt alles rum ;)
            LocalDataBase.setProfilePictureUri(preferences.getUserImage()); //TODO Laureen! Das hier nicht vergessen sonst bugt alles rum ;)

            Toast.makeText(getApplicationContext(),
                    String.format("Willkommen zurück %s!", preferences.getUsername()),
                    Toast.LENGTH_LONG).show();
        }
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }


    public void startApp(View view) {
        onAllPermissionsGranted();
    }
}
