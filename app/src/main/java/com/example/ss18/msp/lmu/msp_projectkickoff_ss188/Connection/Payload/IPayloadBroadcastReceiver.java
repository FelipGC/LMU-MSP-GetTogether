package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Payload;

import com.google.android.gms.nearby.connection.Payload;

public interface IPayloadBroadcastReceiver {
    void createPayloadBroadcastReceiver();
    void onPayloadReceived(Payload payload);
}
