package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl;

import android.location.Location;
import android.nfc.FormatException;

import java.math.BigDecimal;

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
        float dist = a.distanceTo(b);
        return round(dist,1);
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
