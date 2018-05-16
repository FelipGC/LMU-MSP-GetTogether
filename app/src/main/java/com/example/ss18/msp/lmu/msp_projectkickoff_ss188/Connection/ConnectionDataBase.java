package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.MainActivity;
import com.google.android.gms.nearby.Nearby;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

/**
 * Stores everything we need related to the NearbyConnection process.
 */
public class ConnectionDataBase {

    private final String TAG = "ConnectionDataBase";
    private static final ConnectionDataBase connectionDataBase = new ConnectionDataBase();
    /**
     * The connection strategy as defined in https://developers.google.com/nearby/connections/strategies
     */
    private final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    /**
     * The id of the NearbyConnection service. (package name of the mainactivity)
     */
    private String serviceID;

    public static ConnectionDataBase getInstance() {
        return connectionDataBase;
    }

    private ConnectionDataBase() {
    } //( Due to Singleton)

    /**
     * Callbacks for connections to other devices.
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, String.format("onConnectionInitiated(endpointId=%s, endpointName=%s)",
                            endpointId, connectionInfo.getEndpointName()));
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    Log.i(TAG,String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result));
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Log.i(TAG,"Disconnected from endpoint " + endpointId);
                    if (establishedConnections.containsKey(endpointId))
                        establishedConnections.remove(endpointId);
                }
            };
    /**
     * Callback for payloads (data) sent from another device to us.
     */
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    Log.i(TAG, String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        Log.i(TAG, "Payload data fully received!");
                    }
                }
            };


    /**
     * Handler to Nearby Connections.
     */
    private ConnectionsClient connectionsClient;

    /**
     * Currently discovered devices near us.
     */
    private final HashMap<String, ConnectionEndpoint> discoveredEndpoints = new HashMap<>();

    /**
     * All devices we are currently connected to.
     */
    private final HashMap<String, ConnectionEndpoint> establishedConnections = new HashMap<>();

    /**
     * Starts advertising to be spotted by discoverers
     */
    public void startAdvertising() {
        Log.i(TAG, "Starting advertising...");
        //TODO: Implement
    }

     /**
     * Start the process of detecting nearby devices (connectors)
     */
    public void startDiscovering() {
        Log.i(TAG, "Starting discovering...");
        //Clear list everytime we try to re-discover
        discoveredEndpoints.clear();
        //Callbacks for finding devices
        //Finds nearby devices and stores them in "discoveredEndpoints"
        final EndpointDiscoveryCallback endpointDiscoveryCallback =
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        Log.i(TAG, "onEndpointFound: endpoint found, connecting");
                        //Create and define a new ConnectionEndpoint
                        ConnectionEndpoint conecEndpoint = new ConnectionEndpoint(endpointId, info.getEndpointName());
                        discoveredEndpoints.put(conecEndpoint.getId(), conecEndpoint);
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        Log.i(TAG, String.format("onEndpointLost(endpointId=%s)", endpointId));
                    }
                };
        //Start discovering
        connectionsClient.startDiscovery(serviceID, endpointDiscoveryCallback, new DiscoveryOptions(STRATEGY));
    }

    public ConnectionsClient getConnectionsClient() {
        return connectionsClient;
    }

    /**
     * Method to accept or reject a connection as defined in https://developers.google.com/nearby/connections/android/manage-connections
     *
     * @param accept   True if we want to accept the connection
     * @param endPoint The connection endpoint
     */
    public void acceptConnection(boolean accept, ConnectionEndpoint endPoint) {
        if (accept) {
            Log.i(TAG, "Connection ACCEPTED!");
            connectionsClient.acceptConnection(endPoint.getId(), payloadCallback);
        } else {
            Log.i(TAG, "Connection REJECTED!");
            connectionsClient.rejectConnection(endPoint.getId());
        }
    }

    /**
     * Only for discoverers (Presenters)
     * @param endpoint The endpoint to connect
     */
    public void requestConnection(final ConnectionEndpoint endpoint){
       connectionsClient.requestConnection(
                MainActivity.getUserRole().getUserName(),
                endpoint.getId(),
                connectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.

                                //since we are the discoverer and requested the connection, we assume
                                //the discoverers wants to accept to connection rightaway

                                acceptConnection(true,endpoint);
                            }
                        }).addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Nearby Connections failed to request the connection.
                                //TODO: Add some logic?!
                            }
                        });
    }

    /**
     * Defines the connectionClient for the NearbyConnection
     **/
    public void setUpConnectionsClient(MainActivity mainActivity) {
        this.connectionsClient = Nearby.getConnectionsClient(mainActivity);
    }

    public void setServiceId(String serviceId) {
        this.serviceID = serviceId;
    }
}
