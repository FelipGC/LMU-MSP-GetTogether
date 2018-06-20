package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;

import java.util.List;

public class MessageBroadcastReceiver extends BroadcastReceiver {
    private final ConnectionsClient connection;
    private final List<ConnectionEndpoint> connectedEndpoints;

    public MessageBroadcastReceiver(ConnectionsClient connection, List<ConnectionEndpoint> connectedEndpoints) {
        this.connection = connection;
        this.connectedEndpoints = connectedEndpoints;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String data = intent.getStringExtra("DATA");
        for (ConnectionEndpoint endpoint :
                connectedEndpoints) {
            connection.sendPayload(endpoint.getId(), Payload.fromBytes(data.getBytes()));
        }
    }
}
