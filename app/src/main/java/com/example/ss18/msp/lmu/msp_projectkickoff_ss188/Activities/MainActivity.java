package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Presenter;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.Spectator;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;

import java.lang.annotation.Target;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectionDataBase = ConnectionDataBase.getInstance(); //Singleton
        connectionDataBase.setUpConnectionsClient(this);
        connectionDataBase.setServiceId(getPackageName());
    }

    /**
     * Gets executed if the user chooses to be a "Presenter"  by pressing
     * the corresponding button
     * @param view
     */
    public void presenterButtonClicked(View view) {
        Log.i(TAG,"User chose to be a PRESENTER.");
        userRole = new Presenter(userName);
    }
    /**
     * Gets executed if the user chooses to be a "Spectator"  by pressing
     * the corresponding button
     * @param view
     */
    public void spectatorButtonClicked(View view) {
        Log.i(TAG,"User chose to be a SPECTATOR.");
        userRole = new Spectator(userName);
    }

    /**
     * Calls startAdvertising() on the connectionDataBase
     */
    private void startAdvertising(){ connectionDataBase.startAdvertising(); }

     /**
     * Calls startDiscovering() on the connectionDataBase
     */
    private void startDiscovering(){ connectionDataBase.startDiscovering(); }


    //Getters and Setters
    public static User getUserRole() {
        return userRole;
    }

    public static void setUserRole(User userRole) {
        Log.i(TAG,"User changed his role to: " + userRole.getRoleType().toString());
        MainActivity.userRole = userRole;
    }
}
