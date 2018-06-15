package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.location.Location;

public class LocationUtility {
    public static Location getLocationFromBytes(byte[] data){
        Location location = ParcelableUtil.unmarshall(data, Location.CREATOR);
        return location;
    }

    public static byte[] getLocationAsBytes(Location location){
        return ParcelableUtil.marshall(location);
    }

    public static float getDistanceBetween(Location a, Location b){
        return a.distanceTo(b);
    }
}
