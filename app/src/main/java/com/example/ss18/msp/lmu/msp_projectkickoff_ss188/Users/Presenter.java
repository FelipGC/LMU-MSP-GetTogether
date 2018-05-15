package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users;

import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.MainActivity;

/**
 * Sends data to spectators who subscribed.
 */
public class Presenter extends User {
    private final String TAG = "Presenter";
    public Presenter(){
        Log.i(TAG,"Presenter created.");
        roleType = UserRole.PRESENTER;
    }
    @Override
    public void changeRole() {
        MainActivity.setUserRole(new Spectator());
    }
}
