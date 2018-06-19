package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class NearbyDiscoveryService extends AbstractConnectionService {

    /**
     * Callbacks for connections to other devices.
     */
    //Callbacks for finding devices
    //Finds nearby devices and stores them in "discoveredEndpoints"
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(final String endpointId, final DiscoveredEndpointInfo info) {
                    // TODO: First step in handshake
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // TODO
                }
            };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connectionsClient.startDiscovery(serviceID, endpointDiscoveryCallback, new DiscoveryOptions(STRATEGY)).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        // We're discovering!
                        Log.i(TAG, "We are discovering...");
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                                Log.i(TAG, "Something went wrong!");
                                e.printStackTrace();
                            }
                        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectionsClient.stopDiscovery();
    }
}
