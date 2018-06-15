package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.location.Location;
import android.nfc.FormatException;

public class LocationUtility {
    public static Location getLocationFromBytes(byte[] data) throws FormatException{
        Location location = ParcelableUtil.unmarshall(data, Location.CREATOR);
        if(location.getLatitude()==0 && location.getLongitude()==0 && location.getProvider()==null){
            throw new FormatException("byte-Array isn\'t a location");
        }
        return location;
    }

    public static byte[] getLocationAsBytes(Location location){
        return ParcelableUtil.marshall(location);
    }

    public static float getDistanceBetween(Location a, Location b){
        return a.distanceTo(b);
    }
}
