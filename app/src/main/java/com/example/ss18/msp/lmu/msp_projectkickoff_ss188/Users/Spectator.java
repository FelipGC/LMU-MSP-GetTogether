package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users;

import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.MainActivity;

/**
 * A user who (mainly) subscribes and receives data from a spectator.
 */
public class Spectator extends User {
    private final String TAG = "Spectator";
    public Spectator(){
        Log.i(TAG,"Spectator created.");
        roleType = UserRole.SPECTATOR;
    }
    @Override
    public void changeRole() {
        MainActivity.setUserRole(new Presenter());
    }
}
