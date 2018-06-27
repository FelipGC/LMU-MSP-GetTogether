package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Service;
import android.content.Intent;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractConnectionService extends Service implements IService {
    private final String TAG = "AConnectionService";
    private final Map<String, ConnectionEndpoint> pendingEndpoints = new HashMap<>();
    private final Map<String, ConnectionEndpoint> connectedEndpoints = new HashMap<>();
    private final List<ConnectionLifecycleCallback> lifecycleCallbacks = new ArrayList<>();
    private final List<OnMessageListener> messageListeners = new ArrayList<>();
    private final IConnectionMessageFactory messageFactory = new JsonConnectionMessageFactory();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        this.startService();
        return result;
    }

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

        private PayloadCallback fileReceiver = new FileReceiver(messageListeners);
        private PayloadCallback messageReceiver = new MessageReceiver(messageListeners);
        private PayloadCallback streamReceiver = new StreamReceiver(messageListeners);

        @Override
        public void onPayloadReceived(@NonNull String endpointId,
                                      @NonNull Payload payload) {
            fileReceiver.onPayloadReceived(endpointId, payload);
            messageReceiver.onPayloadReceived(endpointId, payload);
            streamReceiver.onPayloadReceived(endpointId, payload);
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId,
                                            @NonNull PayloadTransferUpdate payloadTransferUpdate) {
            fileReceiver.onPayloadTransferUpdate(endpointId, payloadTransferUpdate);
            messageReceiver.onPayloadTransferUpdate(endpointId, payloadTransferUpdate);
            streamReceiver.onPayloadTransferUpdate(endpointId, payloadTransferUpdate);
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
                            //TODO wird aufgerufen, wenn Connection angefragt und Presenter App schließt
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

    @Override
    public void listenMessage(OnMessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    /**
     * Sends an arbitrary text message to all connected devices.
     * @param message The text message. Preferably an Json string for a clean API.
     */
    @Override
    public void broadcastMessage(String message) {
        for (ConnectionEndpoint endpoint :
                connectedEndpoints.values()) {
            sendMessage(endpoint.getId(), message);
        }
    }

    @Override
    public void broadcastStream(ParcelFileDescriptor fileDescriptor) {
        Payload payload = Payload.fromStream(fileDescriptor);
        for (ConnectionEndpoint endpoint :
                connectedEndpoints.values()) {
            connectionsClient.sendPayload(endpoint.getId(), payload);
        }
    }

    @Override
    public void broadcastFile(ParcelFileDescriptor fileDescriptor, String fileName) {
        for (ConnectionEndpoint endpoint :
                connectedEndpoints.values()) {
            sendFile(endpoint.getId(), fileDescriptor, fileName);
        }
    }

    @Override
    public void sendMessage(String endpointId, String message) {
        try {
            byte[] messageData = message.getBytes("UTF-8");
            Payload payload = Payload.fromBytes(messageData);
            connectionsClient.sendPayload(endpointId, payload);
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendFile(String endpointId, ParcelFileDescriptor fileDescriptor, String fileName) {
        Payload filePayload = Payload.fromFile(fileDescriptor);
        long id = filePayload.getId();
        String fileData = messageFactory.buildFileData(id, fileName);
        if (fileData == null) {
            return;
        }
        sendMessage(endpointId, fileData);
        connectionsClient.sendPayload(endpointId, filePayload);
    }

    @Override
    public List<ConnectionEndpoint> getConnectedEndpoints() {
        List<ConnectionEndpoint> list = new ArrayList<>();
        for(ConnectionEndpoint ce : connectedEndpoints.values()){
            list.add(ce);
        }
        return list;
    }

    @Override
    public List<ConnectionEndpoint> getPendingEndpoints() {
        List<ConnectionEndpoint> list = new ArrayList<>();
        for(ConnectionEndpoint ce : pendingEndpoints.values()){
            list.add(ce);
        }
        return list;
    }

    @Override
    public void onDestroy() {
        connectionsClient.stopAllEndpoints();
    }

    protected boolean alreadyConnected(String endpointId) {
        return connectedEndpoints.containsKey(endpointId);
    }

    protected boolean isNotPending(String endpointId) {
        return !pendingEndpoints.containsKey((endpointId));
    }
}
