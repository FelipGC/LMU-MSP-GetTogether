package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ChatFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
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
    private String serviceID = "fjkgldsjf38SAIOsksjd348";

    private final String CHANNEL_ID = "CHANNEL_ID_42";
    private boolean messageFromChat = false;

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
                            //Make sure we dio not lose the connection to the endpoint
                            ConnectionEndpoint endpoint = discoveredEndpoints.get(endpointId);
                            if (endpoint != null)
                                acceptConnection(true, discoveredEndpoints.get(endpointId));
                            break;
                        case PRESENTER:
                            //If we are the presenter, we need to verify if he really
                            //wants to allow the connection to the discoverer (= viewer)

                            //Create endpoint and add it to the list
                            ConnectionEndpoint connectionEndpoint =
                                    new ConnectionEndpoint(endpointId, connectionInfo.getEndpointName());
                            discoveredEndpoints.put(endpointId, connectionEndpoint);
                            displayNotification("Viewer found!", connectionInfo.getEndpointName()
                                    + " is asking for joining your session", NotificationCompat.PRIORITY_DEFAULT);
                            Toast.makeText(getAppLogicActivity(), String.format(String.format("Viewer %s found!",
                                    connectionEndpoint.getName())), Toast.LENGTH_SHORT).show();
                            updateParticipantsCount();
                            break;
                    }
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    Log.i(TAG, String.format("onConnectionResponse(endpointId=%s, result=%s)", endpointId, result.getStatus()));
                    ConnectionEndpoint endpoint = discoveredEndpoints.get(endpointId);
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.i(TAG, "WE ARE CONNECTED");
                            // We're connected! Can now start sending and receiving data.
                            establishedConnections.put(endpointId, discoveredEndpoints.get(endpointId));
                            if (pendingConnections.containsKey(endpointId))
                                pendingConnections.remove(endpointId);
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.i(TAG, "CONNECTION REJECTED");
                            // The connection was rejected by one or both sides.
                            pendingConnections.remove(endpointId);
                            Toast.makeText(appLogicActivity, String.format(String.format("Connection rejected to: %s",
                                    endpoint.getName())), Toast.LENGTH_SHORT).show();
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.i(TAG, "CONNECTION ERROR BEFORE ESTABLISHING");
                            // The connection broke before it was able to be accepted.
                            onDisconnectConsequences(endpoint);
                            break;
                    }
                    updateGUI(endpoint);
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "DISCONNECTED");
                    ConnectionEndpoint endpoint = discoveredEndpoints.get(endpointId);
                    // We've been disconnected from this endpoint. No more data can be
                    // sent or received.
                    onDisconnectConsequences(endpoint);
                }
            };

    /*
     * Sends the received message from the endpoint to the device
     */
    public void onChatMessageSent(String message, Bitmap profilePicture) {
        ChatFragment chat = getAppLogicActivity().getChatFragment();
        chat.getDataFromEndPoint(message, profilePicture);
    }

    /**
     * Executes consequences after a device has disconnected
     */
    private void onDisconnectConsequences(ConnectionEndpoint endpoint) {
        Log.i(TAG, "Disconnected from endpoint " + endpoint.getOriginalName());
        //Clear in this class
        if (pendingConnections.containsKey(endpoint.getId()))
            pendingConnections.remove(endpoint.getId());
        if (establishedConnections.containsKey(endpoint.getId()))
            establishedConnections.remove(endpoint.getId());
        if (discoveredEndpoints.containsKey(endpoint.getId()))
            discoveredEndpoints.remove(endpoint.getId());
        //Clear in other classes
        if (appLogicActivity.getUserRole().getRoleType() == User.UserRole.SPECTATOR)
            appLogicActivity.getSelectPresenterFragment().removeEndpointFromAdapters(endpoint);
        //Update the GUI finally
        updateGUI(endpoint);

    }

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
                        Log.i(TAG, "Received string: " + payloadFilenameMessage);
                        try {
                            int substringDividerIndex = payloadFilenameMessage.indexOf(':');
                            String payloadId = payloadFilenameMessage.substring(0, substringDividerIndex);
                            String filename = payloadFilenameMessage.substring(substringDividerIndex + 1);
                            //We must check wheter we are receiving a file name (in order to rename a file)
                            //or a chat message
                            switch (payloadId) {
                                case "CHAT":
                                    Log.i(TAG, "Received CHAT MESSAGES");
                                    ConnectionEndpoint connectionEndpoint = discoveredEndpoints.get(endpointId);
                                    Bitmap profilePicture = connectionEndpoint.getProfilePicture();
                                    Log.i(TAG, "Received CHAT MESSAGES" + filename + " " + profilePicture);

                                    messageFromChat = true;
                                    onChatMessageSent(filename, profilePicture);
                                    break;
                                default:
                                    Log.i(TAG, "Received FILE NAME");
                                    filePayloadFilenames.put(payloadId, filename);
                                    break;
                            }
                        } catch (Exception e) {
                            return;
                        }
                    } else if (payload.getType() == Payload.Type.FILE) {
                        // Add this to our tracking map, so that we can retrieve the payload later.
                        incomingPayloads.put(endpointId, payload);
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        //Data fully received.
                        Log.i(TAG, "Payload data fully received!");

                        //Check to see if the message is a chat message or a document
                        if (!messageFromChat) {
                            //Display a notification.
                            displayNotification("Document received",
                                    String.format("%s has sent you a document...", establishedConnections.get(endpointId)),
                                    NotificationCompat.PRIORITY_DEFAULT);
                        } else {
                            Log.i("Eli6969696", "chatmessage");
                            displayNotificationChat("Chat message received",
                                    String.format("%s has sent you a message...", establishedConnections.get(endpointId).getName()),
                                    NotificationCompat.PRIORITY_DEFAULT);
                            //messageFromChat = false;
                        }

                        Payload payload = incomingPayloads.get(update.getPayloadId());
                        Log.i(TAG, "Payload is:" + payload + incomingPayloads + filePayloadFilenames);

                        if (payload != null) {
                            //Load data
                            if (payload.getType() == Payload.Type.FILE) {
                                // Retrieve the filename and corresponding payload.
                                File payloadFile = payload.asFile().asJavaFile();
                                String fileName = filePayloadFilenames.remove(endpointId);
                                Log.i(TAG, "Payload name: " + payloadFile.getName());
                                ConnectionEndpoint connectionEndpoint = discoveredEndpoints.get(endpointId);
                                //Update inbox-fragment.
                                appLogicActivity.getInboxFragment().storePayLoad(connectionEndpoint,fileName, payloadFile);
                            }
                        }
                    } else if (update.getStatus() == PayloadTransferUpdate.Status.FAILURE) {
                        Log.i(TAG, "Payload status: PayloadTransferUpdate.Status.FAILURE");
                    }
                }
            };


    /**
     * Displays a notification message.
     * See @see <a>https://developer.android.com/training/notify-user/build-notification>this</a>
     * for more information
     *
     * @param title   The title of the not
     * @param message The message we want to display
     */
    public void displayNotification(final String title, final String message, final int priority) {
        Log.i(TAG, "NOTIFICATION: " + message);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getAppLogicActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.file_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(priority);
        mBuilder.build();
        NotificationManager mNotificationManager = (NotificationManager) getAppLogicActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(42, mBuilder.build());
    }

    /**
     * Displays a notification message for the chat.
     * See @see <a>https://developer.android.com/training/notify-user/build-notification>this</a>
     * for more information
     *
     * @param title   The title of the not
     * @param message The message we want to display
     */
    public void displayNotificationChat(final String title, final String message, final int priority) {
        Log.i(TAG, "NOTIFICATION: " + message);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getAppLogicActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.chat_icon)
                .setContentTitle(title)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(priority);
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
        Log.i(TAG, "Starting advertising..." + "  " + AppLogicActivity.getUserRole().getUserName() + serviceID);
        //Clear list every time we try to re-discover
        reset();
        // Note: Advertising may fail
        connectionsClient.startAdvertising(
                getMergedNameBitmap(), serviceID, connectionLifecycleCallback,
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
     * Clears and resets everything needed for re-advertising or re-discovering
     */
    private void reset() {
        Log.i(TAG, "Resetting connection.");
        //Disconnect from any potential connections and stop advertising/discovering
        terminateConnection();
        //Clear list every time we try to re-discover
        discoveredEndpoints.clear();
        pendingConnections.clear();
        establishedConnections.clear();
    }

    /**
     * Start the process of detecting nearby devices (connectors)
     */
    public void startDiscovering() {
        Log.i(TAG, "Starting discovering as: " + AppLogicActivity.getUserRole().getUserName() + "  " + serviceID);
        reset();
        //Callbacks for finding devices
        //Finds nearby devices and stores them in "discoveredEndpoints"
        final EndpointDiscoveryCallback endpointDiscoveryCallback =
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                        Log.i(TAG, String.format("onEndpointFound(endpointId = %s,endpointName = %s)", endpointId, info.getEndpointName()));
                        //Create and define a new ConnectionEndpoint
                        ConnectionEndpoint connectionEndpoint = new ConnectionEndpoint(endpointId, info.getEndpointName());
                        discoveredEndpoints.put(connectionEndpoint.getId(), connectionEndpoint);
                        displayNotification("Presenter found!", info.getEndpointName()
                                + " can be added to the presentation", NotificationCompat.PRIORITY_LOW);
                        updatePresenters(connectionEndpoint);
                    }

                    @Override
                    public void onEndpointLost(String endpointId) {
                        Log.i(TAG, String.format("onEndpointLost(endpointId=%s)", endpointId));
                        ConnectionEndpoint connectionEndpoint = discoveredEndpoints.get(endpointId);
                        if (discoveredEndpoints.containsKey(endpointId))
                            discoveredEndpoints.remove(endpointId);
                        if (establishedConnections.containsKey(endpointId))
                            establishedConnections.remove(endpointId);
                        if (pendingConnections.containsKey(endpointId))
                            pendingConnections.remove(endpointId);
                        updateGUI(connectionEndpoint);
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
    public void acceptConnectionIfPending(ConnectionEndpoint endPoint) {
        if (!establishedConnections.containsKey(endPoint.getId())) {
            boolean acceptConnection = pendingConnections.containsKey(endPoint.getId());
            acceptConnection(acceptConnection, pendingConnections.get(endPoint.getId()));
        } else establishedConnections.remove(endPoint.getId());
    }

    /**
     * Puts the name together with the mapmap as string into one new string
     */
    private String getMergedNameBitmap(){
        Log.i(TAG , "Result of mergeNameBitmap is: " + AppLogicActivity.getUserRole().getUserName() + ":" + LocalDataBase.getProfilePictureAsString());
        return AppLogicActivity.getUserRole().getUserName() + ":" + LocalDataBase.getProfilePictureAsString();
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
        Log.i(TAG, String.format("Requesting connection for (endpointId=%s, endpointName=%s)",
                endpoint.getId(), endpoint.getName()));
        connectionsClient.requestConnection(
                getMergedNameBitmap(),
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

    public void disconnectFromEndpoint(String endpointID) {
        Log.i(TAG, "Disconnect " + endpointID);
        connectionsClient.disconnectFromEndpoint(endpointID);
        onDisconnectConsequences(discoveredEndpoints.get(endpointID));
    }

    public void disconnectFromAllEndpoints() {
        for (String id : discoveredEndpoints.keySet()) {
            disconnectFromEndpoint(id);
        }
    }

    private void updateParticipantsCount() {
        appLogicActivity.updateParticipantsGUI(establishedConnections.size(), discoveredEndpoints.size());
    }

    /**
     * Sends a Payload object out to all endPointss
     */
    public void sendPayload(Payload payload, String payloadStoringName) {
        for (String endpointId : establishedConnections.keySet()) {
            try {
                Log.i(TAG, "sendPayload to: " + endpointId);
                sendPayload(endpointId, payload, payloadStoringName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the GUI depending on the role (viewer or presenter)
     */
    private void updateGUI(ConnectionEndpoint endpoint) {
        switch (AppLogicActivity.getUserRole().getRoleType()) {
            case SPECTATOR:
                updatePresenters(endpoint);
                break;
            case PRESENTER:
                updateParticipantsCount();
                break;
        }
    }

    /**
     * Sends a Payload object out to one specific endPoint
     */
    public void sendPayload(String endpointId, Payload payload, String payloadStoringName) throws UnsupportedEncodingException {
        Log.i(TAG, "Sent: " + payload.getId() + "with type: " + payload.getType() + " to: " + endpointId);
        // Send the name of the payload/file as a bytes payload first!
        Nearby.getConnectionsClient(appLogicActivity).sendPayload(
                endpointId, Payload.fromBytes(payloadStoringName.getBytes("UTF-8")));
        //Send the payload data afterwards!
        Nearby.getConnectionsClient(appLogicActivity).sendPayload(endpointId, payload);
        //Add to receivedPayLoadData in our data
        LocalDataBase.sentPayLoadData.put(payload.getId(), payload);
    }

    /**
     * Disconnects from all endpoints and stops advertising/discovering
     */
    public void terminateConnection(){
        disconnectFromAllEndpoints();
        switch (appLogicActivity.getUserRole().getRoleType()) {
            case SPECTATOR:
                stopDiscovering();
                break;
            case PRESENTER:
                stopAdvertising();
                break;
        }
    }
    /**
     * Updates the presenters which are available
     */
    public void updatePresenters(ConnectionEndpoint endpoint) {
        appLogicActivity.updatePresentersGUI(endpoint);
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
