package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.BroadcastReceivers.MessageBroadcastReceiver;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConnectionService extends Service {

    protected ConnectionsClient connectionsClient;
    private List<BroadcastReceiver> broadcastReceivers = new ArrayList<>();
    private List<ConnectionEndpoint> connectedEndpoints = new ArrayList<>();
    protected final String TAG = "ConnectionManager";

    /**
     * The id of the NearbyConnection service. (package name of the main activity)
     */
    protected final String serviceID = "SERVICE_ID_NEARBY_CONNECTIONS";

    /**
     * The connection strategy as defined in https://developers.google.com/nearby/connections/strategies
     */
    protected final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    @Override
    public void onCreate() {
        super.onCreate();
        addBroadcastReceiver(new MessageBroadcastReceiver(connectionsClient, connectedEndpoints),
                R.string.connection_message_action);
    }

    protected void addBroadcastReceiver(BroadcastReceiver receiver, int actionId) {
        IntentFilter endpointFoundFilter = new IntentFilter(getString(actionId));
        registerReceiver(receiver, endpointFoundFilter);
        broadcastReceivers.add(receiver);
    }

    @Override
    public void onDestroy() {
        connectionsClient.stopAllEndpoints();
        for (BroadcastReceiver receiver :
                broadcastReceivers) {
            unregisterReceiver(receiver);
        }
    }

    protected void broadcastMessage(String action, String data) {
        if (data == null) {
            return;
        }
        Intent in = new Intent(action);
        in.putExtra("DATA", data);
        sendBroadcast(in);
    }
}