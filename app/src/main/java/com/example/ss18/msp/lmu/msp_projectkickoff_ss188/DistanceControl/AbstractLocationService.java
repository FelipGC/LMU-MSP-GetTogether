package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.PayloadSender;

public abstract class AbstractLocationService extends Service {

    protected Intent intent;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        setLocationListener();
        setUpdateDistance();
        setUpdateTime();
        payloadSender = new PayloadSender();
        checkLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    protected LocationManager locationManager = null;
    protected PayloadSender payloadSender;
    protected Location myLocation;
    protected LocationListener listener;

    protected int updateTime;
    protected int updateDistance;

    protected abstract void setLocationListener();
    protected abstract void setUpdateTime();
    protected abstract void setUpdateDistance();

    protected void checkLocation(){
        if(locationManager== null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},);
                // TODO handling missing permission
                return;
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        updateTime, updateDistance, listener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        updateTime,updateDistance,listener);
            }
        }
    }
}
