package de.lmu.msp.gettogether.Users;

import android.util.Log;

import de.lmu.msp.gettogether.Activities.AppLogicActivity;

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
        AppLogicActivity.setUserRole(new Spectator());
    }
}
