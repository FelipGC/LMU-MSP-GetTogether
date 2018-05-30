package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Stores everything we need related to the NearbyConnection process.
 */
public class ConnectionManager {

    private final String TAG = "ConnectionManager";
    private static final ConnectionManager CONNECTION_MANAGER = new ConnectionManager();
    /**
     * The connection strategy as defined in https://developers.google.com/nearby/connections/strategies
     */
    private final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    /**
     * The id of the NearbyConnection service. (package name of the main activity)
     */
    private String serviceID;

    private final String CHANNEL_ID = "CHANNEL_ID_42";
    public static ConnectionManager getInstance() {
        return CONNECTION_MANAGER;
    }

    /**
     * A reference to the corresponding activity
     */
    private static AppLogicActivity appLogicActivity;

    private ConnectionManager() {
    } //( Due to Singleton)

    /**
     * Callbacks for connections to other devices.
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                //We have received a connection request. Now both sides must either accept or reject the connection.
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, String.format("onConnectionInitiated(endpointId=%s, endpointName=%s)",
                            endpointId, connectionInfo.getEndpointName()));
                    pendingConnections.remove(endpointId);
                    switch (AppLogicActivity.getUserRole().getRoleType()) {

                        case SPECTATOR:
                            //If we are the discoverer (= viewer) and since we requested the connection, we assume
                            //we want to accept to connection anyway
                            acceptConnection(true, discoveredEndpoints.get(endpointId));
                            break;
                        case PRESENTER:
                            //If we are the presenter, we need to verify if he really
                            //wants to allow the connection to the discoverer (= viewer)

                            //Create endpoint and add it to the list
                            ConnectionEndpoint connectionEndpoint =
                                    new ConnectionEndpoint(endpointId, connectionInfo.getEndpointName());
                            discoveredEndpoints.put(endpointId, connectionEndpoint);
                            displayNotification("Viewer found!",connectionInfo.getEndpointName()
                                    + " is asking for joing your session",NotificationCompat.PRIORITY_DEFAULT);
                            updateParticipantsCount();
                            break;
                    }
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    Log.i(TAG, String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result.getStatus()));
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            // We're connected! Can now start sending and receiving data.
                            establishedConnections.put(endpointId, discoveredEndpoints.get(endpointId));
                            if(pendingConnections.containsKey(endpointId))
                                pendingConnections.remove(endpointId);
                            switch (AppLogicActivity.getUserRole().getRoleType()) {
                                case SPECTATOR:
                                    break;
                                case PRESENTER:
                                    updateParticipantsCount();
                                    break;
                            }
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            // The connection was rejected by one or both sides.
                            pendingConnections.remove(endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            // The connection broke before it was able to be accepted.
                            pendingConnections.remove(endpointId);
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    Log.i(TAG, "Disconnected from endpoint " + endpointId);
                    if(pendingConnections.containsKey(endpointId))
                        pendingConnections.remove(endpointId);
                    if (establishedConnections.containsKey(endpointId)) {
                        establishedConnections.remove(endpointId);
                        switch (AppLogicActivity.getUserRole().getRoleType()) {
                            case SPECTATOR:
                                updatePresenters();
                                break;
                            case PRESENTER:
                                updateParticipantsCount();
                                break;
                        }
                    }
                }
            };
    /**
     * Callback for payloads (data) sent from another device to us.
     */
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
            //SimpleArrayMap is a more efficient data structure when lots of changes occur (in comparision to hash map)
            private final SimpleArrayMap<String, Payload> incomingPayloads = new SimpleArrayMap<>();
            private final SimpleArrayMap<String, String> filePayloadFilenames = new SimpleArrayMap<>();
            //Note: onPayloadReceived() is called when the first byte of a Payload is received;
            //it does not indicate that the entire Payload has been received.
            //The completion of the transfer is indicated when onPayloadTransferUpdate() is called with a status of PayloadTransferUpdate.Status.SUCCESS
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
                        }
                        //Extracts the payloadId and filename from the message and stores it in the
                        //filePayloadFilenames map. The format is payloadId:filename.
                        int substringDividerIndex = payloadFilenameMessage.indexOf(':');
                        String payloadId = payloadFilenameMessage.substring(0, substringDividerIndex);
                        String filename = payloadFilenameMessage.substring(substringDividerIndex + 1);
                        filePayloadFilenames.put(payloadId, filename);
                    }else if (payload.getType() == Payload.Type.FILE) {
                        // Add this to our tracking map, so that we can retrieve the payload later.
                        incomingPayloads.put(endpointId, payload);
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        //Data fully received.
                        Log.i(TAG, "Payload data fully received!");
                        //Display a notification.
                        displayNotification("Document received",
                                String.format("%s has sent you a document...",establishedConnections.get(endpointId)),
                                NotificationCompat.PRIORITY_DEFAULT);
                        Payload payload = incomingPayloads.get(update.getPayloadId());
                        //Load data
                        if (payload.getType() == Payload.Type.FILE) {
                            // Retrieve the filename and corresponding payload.
                            File payloadFile = payload.asFile().asJavaFile();
                            String fileName = filePayloadFilenames.remove(endpointId);
                            Log.i(TAG, "Payload name: " + payloadFile.getName());
                            //Update inbox-fragment.
                            appLogicActivity.getInboxFragment().storePayLoad(fileName,payloadFile);
                        }
                    }
                    else if(update.getStatus() == PayloadTransferUpdate.Status.FAILURE){
                        Log.i(TAG, "Payload status: PayloadTransferUpdate.Status.FAILURE");
                    }
                }
            };

    /**
     * Displays a notification message.
     * See @see <a>https://developer.android.com/training/notify-user/build-notification>this</a>
     * for more information
     * @param title The title of the not
     * @param message The message we want to display
     */
    public void displayNotification(final String title, final String message, final int priority){
        Log.i(TAG,"NOTIFICATION: " + message);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getAppLogicActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.file_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority);
        //TODO: ADD VIBRATION AND SOUND!
        //...
        mBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) getAppLogicActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(42, mBuilder.build());
    }
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
     * All devices we want to connect with (for the discoverer)
     */
    private final HashMap<String, ConnectionEndpoint> pendingConnections = new HashMap<>();
    /**
     * Starts advertising to be spotted by discoverers (= viewers)
     */
    public void startAdvertising() {
        Log.i(TAG, "Starting advertising..." +"  "+ AppLogicActivity.getUserRole().getUserName() + serviceID);
        //Clear list every time we try to re-discover
        establishedConnections.clear();
        discoveredEndpoints.clear();
        pendingConnections.clear();
        // Note: Advertising may fail
        connectionsClient.startAdvertising(
                AppLogicActivity.getUserRole().getUserName(), serviceID, connectionLifecycleCallback,
                new AdvertisingOptions(STRATEGY)).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        // We're advertising!
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start advertising.
                            }
                        });
        ;
    }

    /**
     * Start the process of detecting nearby devices (connectors)
     */
    public void startDiscovering() {
        Log.i(TAG, "Starting discovering as: "+ AppLogicActivity.getUserRole().getUserName() +"  "+ serviceID);
        //Clear list every time we try to re-discover
        discoveredEndpoints.clear();
        pendingConnections.clear();
        establishedConnections.clear();
        //Callbacks for finding devices
        //Finds nearby devices and stores them in "discoveredEndpoints"
        final EndpointDiscoveryCallback endpointDiscoveryCallback =
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        Log.i(TAG, String.format("onEndpointFound(endpointId = %s,endpointName = %s)", endpointId,info.getEndpointName()));
                        //Create and define a new ConnectionEndpoint
                        ConnectionEndpoint connectionEndpoint = new ConnectionEndpoint(endpointId, info.getEndpointName());
                        discoveredEndpoints.put(connectionEndpoint.getId(), connectionEndpoint);
                        displayNotification("Presenter found!",info.getEndpointName()
                                + " can be added to the presentation",NotificationCompat.PRIORITY_LOW);
                        updatePresenters();
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        Log.i(TAG, String.format("onEndpointLost(endpointId=%s)", endpointId));
                        if(discoveredEndpoints.containsKey(endpointId))
                            discoveredEndpoints.remove(endpointId);
                        updatePresenters();
                    }
                };
        //Start discovering
        connectionsClient.startDiscovery(serviceID, endpointDiscoveryCallback, new DiscoveryOptions(STRATEGY)).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        // We're discovering!
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start discovering.
                            }
                        });
    }

    /**
     * Stops looking for new devices/endpoints to connect to
     */
    public void stopDiscovering() {
    }

    /**
     * Stops advertising to new discoverers
     */
    public void stopAdvertising() {
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
     * Calls ({@link package.class#requestConnection}) calls accept/reject connection the for
     * every device inside ({@link package.class#pendingConnections}) pendingConnections
     * if not already established
     */
    public void acceptConnectionIfPending(ConnectionEndpoint endPoint){
            if (!establishedConnections.containsKey(endPoint.getId())){
                boolean acceptConnection = pendingConnections.containsKey(endPoint.getId());
                acceptConnection(acceptConnection,pendingConnections.get(endPoint.getId()));
            }
            else establishedConnections.remove(endPoint.getId());
            }
    /**
     * Only for discoverers (Viewers)
     * If the advertisers wishes to establish a connection to a presenter (advertiser), then a connection is needed.
     * According to the documentation, both sides must explicitly accept the connection. Therefor we
     * must first request a connection to the endpoint
     *
     * @param endpoint The endpoint to connect
     */
    public void requestConnection(final ConnectionEndpoint endpoint) {
        Log.i(TAG,String.format("Requesting connection for (endpointId=%s, endpointName=%s)",
                endpoint.getId(), endpoint.getName()));
        connectionsClient.requestConnection(
                AppLogicActivity.getUserRole().getUserName(),
                endpoint.getId(),
                connectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.
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
    public void setUpConnectionsClient(AppLogicActivity appLogicActivity) {
        this.appLogicActivity = appLogicActivity;
        this.connectionsClient = Nearby.getConnectionsClient(appLogicActivity);
    }

    public void setServiceId(String serviceId) {
        this.serviceID = serviceId;
    }

    public void disconnectFromAllEndpoints() {
        //TODO: Implement
        //...
        //updateParticipantCount();
    }
    private void updateParticipantsCount(){
        appLogicActivity.updateParticipantsGUI(establishedConnections.size(),discoveredEndpoints.size());
    }

    /**
     * Sends a Payload object out to all endPointss
     */
    public void sendPayload(Payload payload,String payloadStoringName){
        for (String endpointId : establishedConnections.keySet())
            sendPayload(endpointId,payload,payloadStoringName);
    }
    /**
     * Sends a Payload object out to one specific endPoint
     */
    public void sendPayload(String endpointId, Payload payload, String payloadStoringName){
        Log.i(TAG,"Sent: " + payload.getId() + "with type: " + payload.getType() + " to: " + endpointId);
        Nearby.getConnectionsClient(appLogicActivity).sendPayload(endpointId, payload);
        //Add to receivedPayLoadData in our data
        LocalDataBase.sentPayLoadData.put(payload.getId(),payload);
    }

    /**
     * Updates the presenters which are available
     */
    public void updatePresenters(){
        appLogicActivity.updatePresentersGUI();
    }
    public HashMap<String, ConnectionEndpoint> getDiscoveredEndpoints() {
        return discoveredEndpoints;
    }

    public HashMap<String, ConnectionEndpoint> getEstablishedConnections() {
        return establishedConnections;
    }

    public HashMap<String, ConnectionEndpoint> getPendingConnections() {
        return pendingConnections;
    }

    public static AppLogicActivity getAppLogicActivity() {
        return appLogicActivity;
    }
}
