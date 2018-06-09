package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class ProfilePictureManager {


    private final String TAG = "ProfilePictureManager";
    /**
     * The connection strategy as defined in https://developers.google.com/nearby/connections/strategies
     */
    private final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    /**
     * The id of the NearbyConnection service.
     */
    private final String serviceID = getClass().getCanonicalName();

    private boolean presenter;

    private static final ProfilePictureManager profilePictureManager = new ProfilePictureManager();

    public static ProfilePictureManager getInstance() {
        return profilePictureManager;
    }

    private ProfilePictureManager() {
    }

    public void start(boolean presenter) {
        connectionsClient = Nearby.getConnectionsClient(ConnectionManager.getAppLogicActivity());
        reset();
        this.presenter = presenter;
        if (presenter)
            startAdvertising();
        else startDiscovering();
    }

    /**
     * Start the process of detecting nearby devices (connectors)
     */
    private void startDiscovering() {
        Log.i(TAG, "Starting discovering as: " + AppLogicActivity.getUserRole().getUserName() + "  " + serviceID);
        //Callbacks for finding devices
        //Finds nearby devices and stores them in "discoveredEndpoints"
        final EndpointDiscoveryCallback endpointDiscoveryCallback =
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(final String endpointId, final DiscoveredEndpointInfo info) {
                        Log.i(TAG, String.format("discovererOnEndpointFound(endpointId = %s,endpointName = %s)", endpointId, info.getEndpointName()));
                        requestConnection(endpointId);
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        Log.i(TAG, String.format("onEndpointLost(endpointId=%s)", endpointId));
                    }
                };
        //Start discovering
        connectionsClient.startDiscovery(serviceID, endpointDiscoveryCallback, new DiscoveryOptions(STRATEGY));
    }

    /**
     * Stops looking for new devices/endpoints to connect to
     */
    private void stopDiscovering() {
        connectionsClient.stopDiscovery();
    }

    /**
     * Callbacks for connections to other devices.
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                //We have received a connection request. Now both sides must either accept or reject the connection.
                public void onConnectionInitiated(final String endpointId, final ConnectionInfo connectionInfo) {
                    Log.i(TAG, String.format("onConnectionInitiated(endpointId=%s, endpointName=%s)",
                            endpointId, connectionInfo.getEndpointName()));
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    Log.i(TAG, String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result.getStatus()));
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.i(TAG, "Ready to send profile picture!");
                            try {
                                sendPayload(endpointId, bitmapToBytesPayload(LocalDataBase.getProfilePicture()));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            return;

                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "DISCONNECTED");
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    onDisconnectConsequences(endpointId);
                }
            };


    /**
     * Executes consequences after a device has disconnected
     */
    private void onDisconnectConsequences(String id) {
        Log.i(TAG, "Disconnected from endpoint " + id);
        if (discoveredEndpoints.containsKey(id))
            discoveredEndpoints.remove(id);
    }

    /**
     * Callback for payloads (data) sent from another device to us.
     */
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    //We will be receiving data
                    Log.i(TAG, String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
                    if (payload.getType() == Payload.Type.BYTES) {
                        String payloadFilenameMessage = null;
                        try {
                            payloadFilenameMessage = new String(payload.asBytes(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            return;
                        }
                        if (presenter) {
                            //Add to own database
                            Bitmap bitmap = LocalDataBase.stringToBitmap(payloadFilenameMessage);
                            LocalDataBase.addBitmapToUser(endpointId, bitmap);
                            //Send to others
                            sendOneBitmapToAllEndpoint(endpointId, payload);
                            sendAllBitmapsToOneEndpoint(endpointId);
                        } else {
                            //We received a profile picture
                            Log.i(TAG, String.format("Profile picture received from endpointId=%s", endpointId));
                                int substringDividerIndex = payloadFilenameMessage.indexOf(':');
                                String id;
                                if(substringDividerIndex == -1)
                                    id = endpointId;
                                else
                                    id = payloadFilenameMessage.substring(0, substringDividerIndex);
                                String fileContent = payloadFilenameMessage.substring(substringDividerIndex + 1);
                                Bitmap bitmap = LocalDataBase.stringToBitmap(fileContent);
                                LocalDataBase.addBitmapToUser(id, bitmap);
                        }
                    }
                }

                //We will only receive bytes here, so this method is useless
                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        //Data fully received.
                        Log.i(TAG, "Payload data fully received! ID=" + endpointId);
                    } else if (update.getStatus() == PayloadTransferUpdate.Status.FAILURE) {
                        Log.i(TAG, "Payload status: PayloadTransferUpdate.Status.FAILURE");
                    }
                }
            };

    /**
     * Handler to Nearby Connections.
     */
    private ConnectionsClient connectionsClient;

    /**
     * Currently discovered devices mapped to their profile picture
     */
    private final HashMap<String, Bitmap> discoveredEndpoints = new HashMap<>();


    /**
     * Starts advertising to be spotted by discoverers (= viewers)
     */
    private void startAdvertising() {
        Log.i(TAG, "Starting advertising..." + "  " + AppLogicActivity.getUserRole().getUserName() + serviceID);
        //Clear list every time we try to re-discover
        // Note: Advertising may fail
        connectionsClient.startAdvertising(
                null, serviceID, connectionLifecycleCallback,
                new AdvertisingOptions(STRATEGY));
    }

    /**
     * Clears and resets everything needed for re-advertising or re-discovering
     */
    public void reset() {
        Log.i(TAG, "Resetting connection.");
        //Disconnect from any potential connections and stop advertising/discovering
        disconnectFromAllEndpoints();
        if (presenter)
            stopAdvertising();
        else stopDiscovering();
        //Clear list every time we try to re-discover
        discoveredEndpoints.clear();
    }


    /**
     * Stops looking for new devices/endpoints to connect to
     */
    private void stopAdvertising() {
        connectionsClient.stopAdvertising();
    }

    /**
     *
     */
    private void requestConnection(final String id) {
        Log.i(TAG, String.format("Requesting connection for (endpointId=%s, endpointName=%s)",
                id, null));
        connectionsClient.requestConnection(null, id, connectionLifecycleCallback);
    }

    private void disconnectFromEndpoint(String endpointID) {
        Log.i(TAG, "Disconnect " + endpointID);
        connectionsClient.disconnectFromEndpoint(endpointID);
        onDisconnectConsequences(endpointID);
    }

    private void disconnectFromAllEndpoints() {
        for (String id : discoveredEndpoints.keySet()) {
            disconnectFromEndpoint(id);
        }
    }

    /**
     * Sends a Payload object out to all endPoints
     */
    private void sendOneBitmapToAllEndpoint(String originalSenderID, Payload payload) {
        try {
            String payloadFilenameMessage = new String(payload.asBytes(), "UTF-8");
            payloadFilenameMessage = originalSenderID + ":" + payloadFilenameMessage;
            Payload payloadToSend = bitmapToBytesPayload(LocalDataBase.stringToBitmap(payloadFilenameMessage));
            for (String endpointId : discoveredEndpoints.keySet()) {
                try {
                    Log.i(TAG, "Sending profile picture to: " + endpointId);
                    sendPayload(endpointId, payloadToSend);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a Payload object out to all endPoints
     */
    private void sendAllBitmapsToOneEndpoint(String id) {
        for (String endpointId : discoveredEndpoints.keySet()) {
            Payload payload = bitmapToBytesPayload(discoveredEndpoints.get(endpointId));
            try {
                sendPayload(endpointId, payload);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private Payload bitmapToBytesPayload(final Bitmap bitmap) {
        final String stringBytes = "BITMAP:" + LocalDataBase.getProfilePictureAsString(bitmap);
        final Payload payloadNew = Payload.fromBytes(stringBytes.getBytes());
        return payloadNew;
    }

    /**
     * Sends a Payload object out to one specific endPoint
     */
    private void sendPayload(String endpointId, Payload payload) throws UnsupportedEncodingException {
        Log.i(TAG, "Sent: " + payload.getId() + "with type: " + payload.getType() + " to: " + endpointId);
        //Send the payload data afterwards!
        Nearby.getConnectionsClient(ConnectionManager.getAppLogicActivity()).sendPayload(endpointId, payload);
        //Add to receivedPayLoadData in our data
        LocalDataBase.sentPayLoadData.put(payload.getId(), payload);
    }
}
