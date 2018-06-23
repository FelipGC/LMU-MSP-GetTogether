package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.NearbyDiscoveryService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.MessageFactory;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.ServiceBinder;


import static com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.Constants.MAX_GPS_DISTANCE;

public class CheckDistanceService extends AbstractLocationService implements MessageFactory,ServiceBinder {

    private static String TAG = "CheckDistanceService";

    @Override
    public void onCreate() {
        super.onCreate();
        bindToService();
    }

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
    public String fabricateMessage(String... message) {
        String fabricatedMessage = "DISTANCE:" + message;
        return fabricatedMessage;
    }

    private NearbyDiscoveryService mService;

    @Override
    public void transferFabricatedMessage(String message) {
        String fabricatedMessage = fabricateMessage(message);
        mService.broadcastMessage(fabricatedMessage);
    }
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NearbyDiscoveryService.NearbyDiscoveryBinder binder = (NearbyDiscoveryService.NearbyDiscoveryBinder) service;
            mService = (NearbyDiscoveryService) binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };
    @Override
    public void bindToService() {
        //Bind toService
        Intent intent = new Intent(this, NearbyDiscoveryService.class);
        this.bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }
}
