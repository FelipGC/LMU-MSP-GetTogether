package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.app.Service;
import android.content.Intent;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Payload.IPayloadBroadcastReceiver;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Payload.PayloadBroadcastReceiver;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Payload.SerializablePayload;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

public abstract class AbstractConnectionService extends Service implements IPayloadBroadcastReceiver {

    protected ConnectionsClient connectionsClient;
    protected final String TAG = "ConnectionManager";
    protected PayloadBroadcastReceiver payloadReceiver;

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

    protected void broadcastIntentPayload(Payload payload) {
        Intent in = new Intent("android.intent.action.BROADCAST_PAYLOAD");
        SerializablePayload pS = new SerializablePayload(payload);
        in.putExtra("PAYLOAD",pS);
        sendBroadcast(in);
    }
}
