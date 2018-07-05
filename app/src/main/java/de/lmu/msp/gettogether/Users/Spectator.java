package de.lmu.msp.gettogether.Users;

import android.util.Log;

import de.lmu.msp.gettogether.Activities.AppLogicActivity;

/**
 * A user who (mainly) subscribes and receives data from a spectator.
 */
public class Spectator extends User {
    private final String TAG = "Spectator";

    public Spectator() {
        Log.i(TAG, "Spectator created.");
        roleType = UserRole.SPECTATOR;
    }

    @Override
    public void changeRole() {
        AppLogicActivity.setUserRole(new Presenter());
    }
}
