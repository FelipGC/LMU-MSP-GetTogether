package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.BroadcastReceivers.RequestConnectionBroadcastReceiver;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
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

    private ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(@NonNull String endpoint,
                                                  @NonNull ConnectionInfo info) {
                    // TODO: Second step of Handshake
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId,
                                               @NonNull ConnectionResolution resolution) {
                    // TODO: Third step of Handshake
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    disconnectEndpoint(endpointId);
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
        initBroadcastReceiver();
        startDiscovery();
    }

    private void startDiscovery() {
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

    private void initBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver =
                new RequestConnectionBroadcastReceiver(connectionsClient,
                        connectionLifecycleCallback);
        addBroadcastReceiver(broadcastReceiver, R.string.connection_requestConnection);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectionsClient.stopDiscovery();
    }
}
