package de.lmu.msp.gettogether.DistanceControl;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;


public class FrequentLocationService extends AbstractLocationService {

    private final String TAG = "FrequentLocationService";

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
                Log.i(TAG, "LocationListener::onLocationChanged - new location data available");
                if(payloadSender != null)
                    payloadSender.sendLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i(TAG, "LocationListener::onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i(TAG, "LocationListener::onProviderEnabled - Sending of location possible.");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i(TAG,"LocationListener::onProviderDisabled - Sending of location not possible.");
                //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        };
    }
}
