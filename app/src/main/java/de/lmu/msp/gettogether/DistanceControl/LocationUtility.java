package de.lmu.msp.gettogether.DistanceControl;

import android.location.Location;

import java.math.BigDecimal;

public class LocationUtility {

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
