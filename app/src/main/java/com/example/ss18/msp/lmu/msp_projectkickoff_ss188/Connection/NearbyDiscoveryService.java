package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NearbyDiscoveryService extends AbstractConnectionService implements IDiscoveryService {
    private final String TAG = "DiscoveryService";
    private final Map<String, ConnectionEndpoint> discoveredEndpoints = new HashMap<>();
    private final Map<String,ConnectionEndpoint> pendingEndpoints = new HashMap<>();
    private final IBinder binder = new NearbyDiscoveryBinder();
    private final List<EndpointDiscoveryCallback> discoveryCallbacks = new ArrayList<>();

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
                    for (EndpointDiscoveryCallback callback :
                            discoveryCallbacks) {
                        callback.onEndpointFound(endpointId, info);
                    }
                }

                @Override
                public void onEndpointLost(@NonNull String endpointId) {
                    Log.i(TAG, String.format("onEndpointLost(endpointId=%s)", endpointId));
                    if (!discoveredEndpoints.containsKey(endpointId)) {
                        return;
                    }
                    discoveredEndpoints.remove(endpointId);
                    for (EndpointDiscoveryCallback callback :
                            discoveryCallbacks) {
                        callback.onEndpointLost(endpointId);
                    }
                }
            };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected ConnectionLifecycleCallback initLifecycle() {
        return new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId,
                                              @NonNull ConnectionInfo connectionInfo) {
                if (!discoveredEndpoints.containsKey(endpointId)) {
                    return;
                }
                connectionsClient.acceptConnection(endpointId, payloadCallback);
            }

            @Override
            public void onConnectionResult(@NonNull String s,
                                           @NonNull ConnectionResolution connectionResolution) {
                Log.i(TAG,"onConnectionResult");
            }

            @Override
            public void onDisconnected(@NonNull String s) {
                Log.i(TAG, "onDisconnected");

            }
        };
    }

    @Override
    public void startService() {
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
    public void stopService() {
        connectionsClient.stopDiscovery();
    }


    @Override
    public void listenDiscovery(EndpointDiscoveryCallback callback) {
        discoveryCallbacks.add(callback);
    }

    @Override
    public void requestConnection(final ConnectionEndpoint endpoint) {
        connectionsClient.requestConnection(endpoint.getName(), endpoint.getId(), connectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "We have requested a connection!");
                            }
                        }

                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Something went wrong! requestConnection()");
                                e.printStackTrace();
                            }
                        }
                );
    }

    @Override
    public Iterable<ConnectionEndpoint> getDiscoveredEndpoints() {
        return discoveredEndpoints.values();
    }

    public class NearbyDiscoveryBinder extends Binder {
        public IDiscoveryService getService() {
            return NearbyDiscoveryService.this;
        }
    }
}
