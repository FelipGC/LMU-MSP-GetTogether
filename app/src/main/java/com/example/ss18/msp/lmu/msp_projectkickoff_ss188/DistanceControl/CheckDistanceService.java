package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;

public class CheckDistanceService extends AbstractLocationService {

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
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(CheckDistanceService.this.listener);
                Location locationTo = intent.getParcelableExtra("location");
                if(locationTo == null)
                    return;
                float distance = LocationUtility.getDistanceBetween(location,locationTo);
                NotificationUtility.displayNotification("DEBUG: Distance To Presenter",
                        String.format("distance = %s",distance),
                        NotificationCompat.PRIORITY_DEFAULT);
                if(distance > 10){//TODO
                    payloadSender.sendDistanceWarning(distance);
                }
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
