package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.support.constraint.Constraints.TAG;

public class RequestConnectionBroadcastReceiver extends BroadcastReceiver {
    private final ConnectionsClient connectionsClient;
    private ConnectionLifecycleCallback connectionLifecycleCallback;

    public RequestConnectionBroadcastReceiver(ConnectionsClient connectionsClient,
                                              ConnectionLifecycleCallback connectionLifecycleCallback) {
        this.connectionLifecycleCallback = connectionLifecycleCallback;
        this.connectionsClient = connectionsClient;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectionEndpoint endpoint = ConnectionEndpoint.parseJson(
                intent.getStringExtra("DATA"));
        if (endpoint == null) {
            return;
        }
        String userName = endpoint.getName();
        String id = endpoint.getId();
        connectionsClient.requestConnection(userName, id,
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
                    }
                });
    }
}
