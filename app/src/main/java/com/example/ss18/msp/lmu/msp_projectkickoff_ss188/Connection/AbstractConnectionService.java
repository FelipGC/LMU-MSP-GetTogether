package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Service;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConnectionService extends Service implements IService {
    private final String TAG = "AConnectionService";
    private final Map<String, ConnectionEndpoint> pendingEndpoints = new HashMap<>();
    private final Map<String, ConnectionEndpoint> connectedEndpoints = new HashMap<>();
    private final List<ConnectionLifecycleCallback> lifecycleCallbacks = new ArrayList<>();

    protected ConnectionsClient connectionsClient;
    /**
     * The id of the NearbyConnection service. (package name of the main activity)
     */
    protected final String serviceID = "SERVICE_ID_NEARBY_CONNECTIONS";
    /**
     * The connection strategy as defined in https://developers.google.com/nearby/connections/strategies
     */
    protected final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    protected PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId,
                                      @NonNull Payload payload) {
            // TODO
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId,
                                            @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            // TODO
        }
    };
    protected ConnectionLifecycleCallback serviceSpecificLifecycleCallback;
    protected ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {

                @Override
                public void onConnectionInitiated(@NonNull String endpointId,
                                                  @NonNull ConnectionInfo info) {
                    ConnectionEndpoint endpoint =
                            new ConnectionEndpoint(endpointId, info.getEndpointName());
                    pendingEndpoints.put(endpoint.getId(), endpoint);
                    if (serviceSpecificLifecycleCallback != null) {
                        serviceSpecificLifecycleCallback.onConnectionInitiated(endpointId, info);
                    }
                    for (ConnectionLifecycleCallback callback :
                            lifecycleCallbacks) {
                        callback.onConnectionInitiated(endpointId, info);
                    }
                }

                @Override
                public void onConnectionResult(@NonNull String endpointId,
                                               @NonNull ConnectionResolution resolution) {
                    if (!pendingEndpoints.containsKey(endpointId)) {
                        return;
                    }
                    switch (resolution.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            connectedEndpoints.put(endpointId, pendingEndpoints.get(endpointId));
                            pendingEndpoints.remove(endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            pendingEndpoints.remove(endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.d(TAG, String.format(
                                    "Got ConnectionResolution status Error: %s",
                                    resolution.getStatus().getStatusMessage()));
                            break;
                        default:
                            Log.d(TAG, String.format(
                                    "Got ConnectionResolution StatusCode: %d - %s",
                                    resolution.getStatus().getStatusCode(),
                                    resolution.getStatus().getStatusMessage()));
                            break;
                    }
                    if (serviceSpecificLifecycleCallback != null) {
                        serviceSpecificLifecycleCallback.onConnectionResult(endpointId, resolution);
                    }
                    for (ConnectionLifecycleCallback callback :
                            lifecycleCallbacks) {
                        callback.onConnectionResult(endpointId, resolution);
                    }
                }

                @Override
                public void onDisconnected(@NonNull String endpointId) {
                    if (!connectedEndpoints.containsKey(endpointId)) {
                        return;
                    }
                    connectedEndpoints.remove(endpointId);
                    if (serviceSpecificLifecycleCallback != null) {
                        serviceSpecificLifecycleCallback.onDisconnected(endpointId);
                    }
                    for (ConnectionLifecycleCallback callback :
                            lifecycleCallbacks) {
                        callback.onDisconnected(endpointId);
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        serviceSpecificLifecycleCallback = initLifecycle();
        connectionsClient = Nearby.getConnectionsClient(this);
    }

    protected abstract ConnectionLifecycleCallback initLifecycle();

    public void listenLifecycle(ConnectionLifecycleCallback connectionLifecycleCallback) {
        lifecycleCallbacks.add(connectionLifecycleCallback);
    }

    /**
     * Sends an arbitrary text message to all connected devices.
     * @param message The text message. Preferably an Json string for a clean API.
     */
    @Override
    public void broadcastMessage(String message) {
        Payload payload = Payload.fromBytes(message.getBytes()); // One message per send or one for all?
        sendPayload(payload);
    }

    @Override
    public void broadcastStream(ParcelFileDescriptor fileDescriptor) {
        Payload payload = Payload.fromStream(fileDescriptor);
        sendPayload(payload);
    }

    @Override
    public void broadcastFile(ParcelFileDescriptor fileDescriptor) {
        Payload payload = Payload.fromFile(fileDescriptor);
        sendPayload(payload);
    }

    @Override
    public void sendFile(String endpointId, ParcelFileDescriptor fileDescriptor) {
        Payload payload = Payload.fromFile(fileDescriptor);
        connectionsClient.sendPayload(endpointId, payload);
    }

    private void sendPayload(Payload payload) {
        for (ConnectionEndpoint endpoint :
                connectedEndpoints.values()) {
            connectionsClient.sendPayload(endpoint.getId(), payload);
        }
    }

    @Override
    public Iterable<ConnectionEndpoint> getConnectedEndpoints() {
        return connectedEndpoints.values();
    }

    @Override
    public Iterable<ConnectionEndpoint> getPendingEndpoints() {
        return pendingEndpoints.values();
    }

    @Override
    public void onDestroy() {
        connectionsClient.stopAllEndpoints();
    }

    protected boolean alreadyConnected(String endpointId) {
        return connectedEndpoints.containsKey(endpointId);
    }

    protected boolean isPending(String endpointId) {
        return pendingEndpoints.containsKey((endpointId));
    }
}
