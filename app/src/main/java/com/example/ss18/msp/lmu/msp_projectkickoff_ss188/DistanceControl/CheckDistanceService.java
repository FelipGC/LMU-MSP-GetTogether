package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.MessageFactory;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;
import com.google.android.gms.nearby.connection.Payload;

import static com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.Constants.MAX_GPS_DISTANCE;

public class CheckDistanceService extends AbstractLocationService implements MessageFactory {

    private static String TAG = "CheckDistanceService";

    @Override
    protected void setUpdateDistance() {
        updateDistance = 0;
    }

    @Override
    protected void setUpdateTime() {
        updateTime = 0;
    }

    @Override
    protected void setLocationListener() {
        listener = new LocationListener() {
            private float lastDistance = -1;
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(CheckDistanceService.this.listener);
                Location locationTo = intent.getParcelableExtra("location");
                if(locationTo == null)
                    return;
                float distance = LocationUtility.getDistanceBetween(location,locationTo);
                Log.i(TAG,"DISTANCE: " + distance);
                if(distance > MAX_GPS_DISTANCE){
                    //TODO: DISPLAY NOTIFICATION
                    NotificationUtility.displayNotification("ACHTUNG",
                            String.format("Distanz zum Moderator: %s m.",distance),
                            NotificationCompat.PRIORITY_DEFAULT);
                }
                //LAUREEM: Wir schicken die Distanz jetzt immer falls sich die position stark ändert (3 Meter)!
                if(lastDistance-distance > 3)
                    transferFabricatedMessage(String.valueOf(distance));
                lastDistance = distance;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    @Override
    public String fabricateMessage(String message) {
        String fabricatedMessage = "DISTANCE:" + message;
        return fabricatedMessage;
    }

    @Override
    public void transferFabricatedMessage(String message) {
        String fabricatedMessage = fabricateMessage(message);
        AppLogicActivity.getInstance().getmService().broadcastMessage(fabricatedMessage);
    }
}
