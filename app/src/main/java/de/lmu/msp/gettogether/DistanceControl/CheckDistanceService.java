package de.lmu.msp.gettogether.DistanceControl;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import de.lmu.msp.gettogether.R;
import de.lmu.msp.gettogether.Utility.NotificationUtility;

import static de.lmu.msp.gettogether.Utility.Constants.MAX_GPS_DISTANCE;

public class CheckDistanceService extends AbstractLocationService {

    private static String TAG = "CheckDistanceService";

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
                //locationManager.removeUpdates(this);
                if(intent==null)return;
                Location locationTo = intent.getParcelableExtra("location");
                intent=null;
                if(locationTo == null)
                    return;
                float distance = LocationUtility.getDistanceBetween(location,locationTo);
                if(distance > MAX_GPS_DISTANCE){
                    NotificationUtility.displayNotification(getString(R.string.distance_warning),
                            getString(R.string.distance_to_moderator, String.valueOf(distance)),
                            NotificationCompat.PRIORITY_DEFAULT);
                }
                payloadSender.sendDistance(distance);
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
