package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import android.os.IBinder;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.NearbyAdvertiseService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.MessageFactory;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.ServiceBinder;

import java.util.Objects;


public class FrequentLocationService extends AbstractLocationService implements MessageFactory,ServiceBinder {

    private final String TAG = "FrequentLocationService";

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
        updateTime = 30 * 1000; //TODO
    }

    @Override
    protected void setLocationListener() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "LocationListener::onLocationChanged - new location data available");
                transferFabricatedMessage(location.getLatitude() + "/" + location.getLongitude());
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

    @Override
    public String fabricateMessage(String... message) {
        String fabricatedMessage = "LOCATION:" + message;
        return fabricatedMessage;
        }

    private NearbyAdvertiseService mService;

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
            NearbyAdvertiseService.NearbyAdvertiseBinder binder = (NearbyAdvertiseService.NearbyAdvertiseBinder) service;
            mService = (NearbyAdvertiseService) binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };
    @Override
    public void bindToService() {
        //Bind toService
        Intent intent = new Intent(this, NearbyAdvertiseService.class);
        Objects.requireNonNull(this).bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }
}
