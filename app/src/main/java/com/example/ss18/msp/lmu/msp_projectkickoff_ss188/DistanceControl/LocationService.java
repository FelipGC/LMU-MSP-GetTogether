package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.PayloadSender;

import java.security.Permission;

public class LocationService extends Service {

    private final String TAG = "LocationService";
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private LocationManager locationManager = null;
    private Location location;
    private PayloadSender payloadSender;

    private void observeLocation(){
        if(locationManager== null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},);
                // TODO handling missing permission
                return;
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        10000, 0, frequentListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,10000,0,frequentListener);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        payloadSender = ConnectionManager.getInstance().getPayloadSender();
        observeLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    private LocationListener frequentListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "LocationListener::onLocationChanged - new location data available");
            //locationManager.removeUpdates(listener);
           // payloadSender.sendLocation(location);
            byte[] b = LocationUtility.getLocationAsBytes(location);
            String s = "blablabla";
            byte[] b2 = s.getBytes();

            //Location loc = LocationUtility.getLocationFromBytes(b2);
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
        }
    };
}
