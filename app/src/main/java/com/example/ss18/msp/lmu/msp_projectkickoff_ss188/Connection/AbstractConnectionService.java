package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver.CombinedPayloadReceiver;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver.OnMessageListener;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Messages.JsonFileDataMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.AppPreferences;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.ChatMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.IMessageDistributionService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.JsonMessageDistributionService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageDistributionBinder;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.SystemMessage;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConnectionService extends Service implements IService {
    private final String TAG = "AConnectionService";
    private final Map<String, ConnectionEndpoint> pendingEndpoints = new HashMap<>();
    private final Map<String, ConnectionEndpoint> connectedEndpoints = new HashMap<>();
    private final List<ConnectionLifecycleCallback> lifecycleCallbacks = new ArrayList<>();
    private final List<OnMessageListener> messageListeners = new ArrayList<>();

    protected CombinedPayloadReceiver payloadReceiver =
            new CombinedPayloadReceiver(messageListeners);

    //private final IConnectionMessageFactory messageFactory = new JsonConnectionMessageFactory();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        this.startService();
        return result;
    }

    @Override
    public void disconnect(String endpointId) {
        connectionsClient.disconnectFromEndpoint(endpointId);
    }

    protected ConnectionsClient connectionsClient;
    protected final String serviceID = "SERVICE_ID_NEARBY_CONNECTIONS";
    protected final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private ServiceConnection messageDistributionServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MessageDistributionBinder messageDistributionBinder =
                    (MessageDistributionBinder) service;
            IMessageDistributionService messageDistributionService =
                    messageDistributionBinder.getService();
            payloadReceiver.setDistributionService(messageDistributionService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            payloadReceiver.unsetDistributionService();
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
                            pendingEndpoints.remove(endpointId);
                            Log.d(TAG, String.format(
                                    "Got ConnectionResolution status Error: %s",
                                    resolution.getStatus().getStatusMessage()));
                            pendingEndpoints.remove(endpointId);
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
        bindMessageListener();
    }

    private void bindMessageListener() {
        Intent intent = new Intent(this, JsonMessageDistributionService.class);
        bindService(intent, messageDistributionServiceConnection, Context.BIND_AUTO_CREATE);
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
    public void broadcastSystemMessage(String content) {
        BaseMessage msg = new SystemMessage(content);
        broadcastMessage(msg.toJsonString());
    }

    @Override
    public void broadcastChatMessage(String msgBody) {
        BaseMessage msg = new ChatMessage(AppPreferences.getInstance().getUsername(), msgBody);
        broadcastMessage(msg.toJsonString());
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
    public void sendMessage(String endpointId, String message) { // TODO: Maybe threaded?
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
    public void sendFile(String endpointId, ParcelFileDescriptor fileDescriptor, String fileName) { // TODO: Maybe threaded?
        Payload filePayload = Payload.fromFile(fileDescriptor);
        long payloadId = filePayload.getId();
        BaseMessage jsonFileDataMessage = new JsonFileDataMessage(payloadId, fileName);
        sendMessage(endpointId, jsonFileDataMessage.toJsonString());
        connectionsClient.sendPayload(endpointId, filePayload);
    }

    @Override
    public Collection<ConnectionEndpoint> getConnectedEndpoints() {
        return new ArrayList<>(connectedEndpoints.values());
    }

    @Override
    public Collection<ConnectionEndpoint> getPendingEndpoints() {
        return new ArrayList<>(pendingEndpoints.values());
    }

    @Override
    public void onDestroy() {
        unbindService(messageDistributionServiceConnection);
        connectionsClient.stopAllEndpoints();
        stopService();
    }

    protected boolean alreadyConnected(String endpointId) {
        return connectedEndpoints.containsKey(endpointId);
    }

    protected boolean isNotPending(String endpointId) {
        return !pendingEndpoints.containsKey((endpointId));
    }

    protected String getNameOfEndpoint(String endpointId){
        if(connectedEndpoints.containsKey(endpointId))
            return connectedEndpoints.get(endpointId).getName();
        return null;
    }
}
