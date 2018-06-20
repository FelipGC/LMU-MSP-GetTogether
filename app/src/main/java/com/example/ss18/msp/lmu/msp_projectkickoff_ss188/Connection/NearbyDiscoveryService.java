package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

public class NearbyDiscoveryService extends AbstractConnectionService {
    private Map<String, ConnectionEndpoint> discoveredEndpoints = new HashMap<>();

    /**
     * Callbacks for connections to other devices.
     */
    //Callbacks for finding devices
    //Finds nearby devices and stores them in "discoveredEndpoints"
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {

                @Override
                public void onEndpointFound(@NonNull String endpointId,
                                            @NonNull DiscoveredEndpointInfo info) {
                    Log.i(TAG, String.format(
                            "discovererOnEndpointFound(endpointId = %s,endpointName = %s)",
                            endpointId, info.getEndpointName()));
                    ConnectionEndpoint connectionEndpoint =
                            new ConnectionEndpoint(endpointId, info.getEndpointName());
                    discoveredEndpoints.put(connectionEndpoint.getId(), connectionEndpoint);
                    NotificationUtility.displayNotification("Presenter found!",
                            info.getEndpointName()
                                    + " can be added to the presentation",
                            NotificationCompat.PRIORITY_LOW);
                    broadcastMessage(getString(R.string.connection_endpointFound),
                            connectionEndpoint.toJsonString());
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.i(TAG, String.format("onEndpointLost(endpointId=%s)", endpointId));
                    ConnectionEndpoint endpoint = discoveredEndpoints.get(endpointId);
                    discoveredEndpoints.remove(endpointId);
                    broadcastMessage(getString(R.string.connection_endpointLost),
                            endpoint.toJsonString());
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
        DiscoveryOptions.Builder builder = new DiscoveryOptions.Builder();
        builder.setStrategy(STRATEGY);
        connectionsClient.startDiscovery(serviceID, endpointDiscoveryCallback, builder.build())
                .addOnSuccessListener(
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
