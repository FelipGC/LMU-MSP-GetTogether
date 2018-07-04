package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl.FrequentLocationService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;
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
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Stores everything we need related to the NearbyConnection process.
 */
public class ConnectionManager extends Service {

    private final String TAG = "ConnectionManager";
    /**
     * The connection strategy as defined in https://developers.google.com/nearby/connections/strategies
     */
    private final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    /**
     * The id of the NearbyConnection service. (package name of the main activity)
     */
    private final String serviceID = "SERVICE_ID_NEARBY_CONNECTIONS";

    PayloadSender payloadSender;


    /**
     * A reference to the corresponding activity
     */
    private static AppLogicActivity appLogicActivity;

    private final IBinder binder = new ConnectionManagerBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class ConnectionManagerBinder extends Binder {
        public ConnectionManager getService() {
            return ConnectionManager.this;
        }
    }

    /**
     * Callbacks for connections to other devices.
     */
    //Callbacks for finding devices
    //Finds nearby devices and stores them in "discoveredEndpoints"
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(final String endpointId, final DiscoveredEndpointInfo info) {
                    Log.i(TAG, String.format("discovererOnEndpointFound(endpointId = %s,endpointName = %s)", endpointId, info.getEndpointName()));
                    ConnectionEndpoint connectionEndpoint = new ConnectionEndpoint(endpointId, info.getEndpointName());
                    //Create and define a new ConnectionEndpoint
                    discoveredEndpoints.put(connectionEndpoint.getId(), connectionEndpoint);
                    NotificationUtility.displayNotification("Presenter found!", info.getEndpointName()
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

    /**
     * Callback for both, presenters and viewers
     */
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                //We have received a connection request. Now both sides must either accept or reject the connection.
                public void onConnectionInitiated(final String endpointId, final ConnectionInfo connectionInfo) {
                    Log.i(TAG, String.format("onConnectionInitiated(endpointId=%s, endpointName=%s)",
                            endpointId, connectionInfo.getEndpointName()));
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
                            if (!LocalDataBase.isAutoConnect()) {
                                NotificationUtility.displayNotification("Teilnehmer gefunden", connectionInfo.getEndpointName()
                                        + " möchte Deiner Gruppe beitreten", NotificationCompat.PRIORITY_DEFAULT);
                            } else {
                                acceptConnection(true, connectionEndpoint);
                            }
                            updateParticipantsCount(connectionEndpoint);
                            Toast.makeText(getAppLogicActivity(), String.format(String.format("Teilnehmer %s gefunden",
                                    connectionEndpoint.getName())), Toast.LENGTH_SHORT).show();
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
                            //Display system message inside the chat window
                            appLogicActivity.getChatFragment().displaySystemNotification(
                                    String.format("%s ist der Gruppe beigetreten.", endpoint.getName()));
                            // We're connected! Can now start sending and receiving data.
                            establishedConnections.put(endpointId, endpoint);
                            if (pendingConnections.containsKey(endpointId))
                                pendingConnections.remove(endpointId);

                            final boolean SPECTATOR = AppLogicActivity.getUserRole().getRoleType() == User.UserRole.SPECTATOR;

                            try {
                                Uri uri = LocalDataBase.getProfilePictureUri();
                                if (uri == null) {
                                    if(SPECTATOR) {
                                        Log.i(TAG, "URI NULL...");
                                        String message = "NULL_PROF_PIC:";
                                        payloadSender.sendPayloadBytesToSpecific(endpointId, Payload.fromBytes(message.getBytes("UTF-8")));
                                    }
                                } else {
                                    //TODO: IMAGE IS TOO BIG?
                                    ParcelFileDescriptor file = appLogicActivity.getContentResolver().openFileDescriptor(uri, "r");
                                    assert file != null;
                                    Payload payload = Payload.fromFile(file);
                                    Log.i(TAG, "Sending prof image: " + payload);
                                    payloadSender.sendPayloadFile(endpointId, payload, payload.getId() + (SPECTATOR ? ":PROF_PIC_V:" : ":PROF_PIC:"));
                                }
                                if (!SPECTATOR) {
                                    appLogicActivity.startService(new Intent(appLogicActivity, FrequentLocationService.class));
                                    for (ConnectionEndpoint otherEndpoint : establishedConnections.values()) {
                                        //Do not send info to the same endpoint
                                        if (otherEndpoint.getId().equals(endpointId))
                                            continue;
                                        //Send all the other viewers to the viewer
                                        sendConnectionEndpointTo(endpointId, otherEndpoint);
                                        //Send this endpoint to all others
                                        sendConnectionEndpointTo(otherEndpoint.getId(), endpoint);

                                    }
                                }

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            //Send missing chat if enabled
                            if (LocalDataBase.isSendMissingChat()) {
                                Log.i(TAG, "isSendMissingChat()");
                                Log.i(TAG, LocalDataBase.chatHistory.toString());
                                for (Payload chatPayload : LocalDataBase.chatHistory) {
                                    if (LocalDataBase.isChatAnonymized())
                                        payloadSender.sendPayloadBytesAnonymizedToSpecific(endpointId, chatPayload);
                                    else
                                        payloadSender.sendPayloadBytesToSpecific(endpointId, chatPayload);
                                }
                            }
                            //Send missing files if enabled
                            if (LocalDataBase.isSendMissingFiles()) {
                                Log.i(TAG, "isSendMissingFiles()");
                                Log.i(TAG, LocalDataBase.urisSent.toString());
                                for (Uri uriToSend : LocalDataBase.urisSent) {
                                    try {
                                        getAppLogicActivity().getShareFragment().sendDataToEndpoint(endpointId, uriToSend);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.i(TAG, "CONNECTION REJECTED");
                            // The connection was rejected by one or both sides.
                            pendingConnections.remove(endpointId);
                            Toast.makeText(appLogicActivity, String.format(String.format("Anfrage von %s abgelehnt",
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

    private void sendConnectionEndpointTo(String endpoint, ConnectionEndpoint otherEndpoint) {
        String stringToSend = String.format("C_ENDPOINT:%s:%s", otherEndpoint.getId(), otherEndpoint.getName());
        Payload payload = Payload.fromBytes(stringToSend.getBytes());
        payloadSender.sendPayloadBytesToSpecific(endpoint, payload);
    }

    /**
     * Executes consequences after a device has disconnected
     */
    private void onDisconnectConsequences(ConnectionEndpoint endpoint) {
        if (endpoint == null)
            return;
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
        appLogicActivity.getChatFragment().displaySystemNotification(
                String.format("%s hat die Gruppe verlassen.", endpoint.getName()));
        //Update the GUI finally
        updateGUI(endpoint);

    }

    /**
     * Callback for payloads (data) sent from another device to us.
     */
    private PayloadCallback payloadCallback;

    /**
     * Handler to Nearby Connections.
     */
    private ConnectionsClient connectionsClient;

    /**
     * Currently discovered devices near us.
     */
    private HashMap<String, ConnectionEndpoint> discoveredEndpoints;

    /**
     * All devices we are currently connected to.
     */
    private HashMap<String, ConnectionEndpoint> establishedConnections;
    /**
     * All devices we want to connect with (for the discoverer)
     */
    private HashMap<String, ConnectionEndpoint> pendingConnections;

    @Override
    public void onCreate() {
        super.onCreate();
        discoveredEndpoints = new HashMap<>();
        establishedConnections = new HashMap<>();
        pendingConnections = new HashMap<>();
    }

    /**
     * Starts advertising to be spotted by discoverers (= viewers)
     */
    public void startAdvertising() {
        Log.i(TAG, "Starting advertising:" + " Name: " + AppLogicActivity.getUserRole().getUserName() + " ServiceID: " + serviceID);
        //Clear list every time we try to re-discover
        reset();
        // Note: Advertising may fail
        new Thread(new Runnable() {
            @Override
            public void run() {
                // a potentially  time consuming task
                connectionsClient.startAdvertising(
                        AppLogicActivity.getUserRole().getUserName(), serviceID, connectionLifecycleCallback,
                        new AdvertisingOptions(STRATEGY)).addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We're advertising!
                                Log.i(TAG, "We are advertising...");
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // We were unable to start advertising.
                                        Log.i(TAG, "Something went wrong!");
                                        e.printStackTrace();
                                    }
                                });
            }
        }).start();

    }

    /**
     * Start the process of detecting nearby devices (connectors)
     */
    public void startDiscovering() {
        Log.i(TAG, "Starting discovering as: " + AppLogicActivity.getUserRole().getUserName() + "  " + serviceID);
        reset();
        //Start discovering
        // Note: Advertising may fail
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectionsClient.startDiscovery(serviceID, endpointDiscoveryCallback, new DiscoveryOptions(STRATEGY)).addOnSuccessListener(
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
        }).start();
    }

    /**
     * Clears and resets everything needed for re-advertising or re-discovering
     */
    private void reset() {
        Log.i(TAG, "Resetting connection.");
        LocalDataBase.resetDataBaseCache();
        //Disconnect from any potential connections and stop advertising/discovering
        disconnectFromAllEndpoints();
        //Clear lists every time we try to re-discover
        LocalDataBase.chatHistory.clear();
        LocalDataBase.urisSent.clear();
        discoveredEndpoints.clear();
        pendingConnections.clear();
        establishedConnections.clear();
    }

    /**
     * Stops looking for new devices/endpoints to connect to
     */
    public void stopDiscovering() {
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
    }

    /**
     * Stops advertising to new discoverers
     */
    public void stopAdvertising() {
        connectionsClient.stopAdvertising();
        connectionsClient.stopAllEndpoints();
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
        if(endPoint == null)
            return;
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
                AppLogicActivity.getUserRole().getUserName(),
                endpoint.getId(),
                connectionLifecycleCallback)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unusedResult) {
                                // We successfully requested a connection. Now both sides
                                // must accept before the connection is established.
                                // We were unable to start advertising.
                                Log.i(TAG, "We have requested a connection!");
                            }
                        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Nearby Connections failed to request the connection.
                        //TODO: Add some logic?!
                        // We were unable to start advertising.
                        Log.i(TAG, "Something went wrong! requestConnection()");
                        e.printStackTrace();
                        if (discoveredEndpoints.containsKey(endpoint.getId())) {
                            Log.i(TAG, "Retrying to connect to: " + endpoint.getName());
                            requestConnection(endpoint);
                        }
                    }
                });
    }

    /**
     * Defines the connectionClient for the NearbyConnection
     **/
    public void setUpConnectionsClient(AppLogicActivity appLogicActivity) {
        Log.i(TAG, "Setting up connection client");
        this.appLogicActivity = appLogicActivity;
        this.connectionsClient = Nearby.getConnectionsClient(appLogicActivity);
        //Define Sender & Receiver
        payloadSender = new PayloadSender();
        payloadCallback = new PayloadReceiver();
    }

    public void disconnectFromEndpoint(String endpointID) {
        Log.i(TAG, "Disconnect " + endpointID);
        ConnectionEndpoint endpoint = discoveredEndpoints.get(endpointID);
        connectionsClient.disconnectFromEndpoint(endpointID);
        onDisconnectConsequences(endpoint);
    }

    public void disconnectFromAllEndpoints() {
        connectionsClient.stopAllEndpoints();
    }

    private void updateParticipantsCount(ConnectionEndpoint e) {
        appLogicActivity.updateParticipantsGUI(e, establishedConnections.size(), discoveredEndpoints.size());
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
                updateParticipantsCount(endpoint);
                break;
        }
    }

    /**
     * Disconnects from all endpoints and stops advertising/discovering
     */
    public void terminateConnection() {
        reset();
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

    /** Returns a cloned version of establishedConnections*/
    public HashMap<String, ConnectionEndpoint> getEstablishedConnectionsCloned() {
        return (HashMap<String, ConnectionEndpoint>) establishedConnections.clone();
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

    public PayloadSender getPayloadSender() {
        return payloadSender;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ConnectionManager-Service DESTROYED!!!!");
        terminateConnection();
        if (appLogicActivity != null) {
            for (ServiceConnection s : appLogicActivity.serviceConnections) {
                unbindService(s);
            }
        }
    }
}
