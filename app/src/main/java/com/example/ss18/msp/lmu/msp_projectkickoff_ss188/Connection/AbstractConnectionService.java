package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Service;

import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

public abstract class AbstractConnectionService extends Service {

    protected ConnectionsClient connectionsClient;
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
    public void onDestroy() {
        connectionsClient.stopAllEndpoints();
    }

    protected void sendNearbyMessage(Payload payload) {
        // ... just for testing ...
        // connectionsClient.sendPayload(id, payload);
    }
}
