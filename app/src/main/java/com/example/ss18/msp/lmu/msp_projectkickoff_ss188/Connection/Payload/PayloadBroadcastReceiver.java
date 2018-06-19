package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Payload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.nearby.connection.Payload;

public class PayloadBroadcastReceiver extends BroadcastReceiver {
    IPayloadBroadcastReceiver receiver;
    public PayloadBroadcastReceiver(IPayloadBroadcastReceiver receiver){
        this.receiver = receiver;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Payload payload = ((SerializablePayload) intent.getSerializableExtra("PAYLOAD")).p;
        receiver.onPayloadReceived(payload);
    }
}
