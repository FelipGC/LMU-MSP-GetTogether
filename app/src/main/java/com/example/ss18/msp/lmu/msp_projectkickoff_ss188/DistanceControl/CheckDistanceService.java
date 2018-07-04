package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;

import static com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.Constants.MAX_GPS_DISTANCE;

public class CheckDistanceService extends AbstractLocationService {

    private static String TAG = "CheckDistanceService";

    @Override
    protected void setUpdateDistance() {
        updateDistance = 0;
    }

    @Override
    protected void setUpdateTime() {
        updateTime = 30 * 1000;
    }

    @Override
    protected void setLocationListener() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(this);
                Location locationTo = intent.getParcelableExtra("location");
                if(locationTo == null)
                    return;
                float distance = LocationUtility.getDistanceBetween(location,locationTo);
                if(distance > MAX_GPS_DISTANCE){
                    //TODO: DISPLAY NOTIFICATION
                    NotificationUtility.displayNotification("ACHTUNG",
                            String.format("Distanz zum Moderator: %s m.",distance),
                            NotificationCompat.PRIORITY_DEFAULT);
                }
                payloadSender.sendDistance(distance);
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
}
