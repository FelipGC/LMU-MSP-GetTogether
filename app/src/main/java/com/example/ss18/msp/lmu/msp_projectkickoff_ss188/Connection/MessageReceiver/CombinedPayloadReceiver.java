package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.support.annotation.NonNull;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.IMessageDistributionService;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.util.List;

public class CombinedPayloadReceiver extends PayloadCallback {
    private final FileReceiver fileReceiver;
    private final MessageReceiver messageReceiver;
    private final PayloadCallback streamReceiver;

    public CombinedPayloadReceiver(List<IOnMessageListener> messageListeners) {
        fileReceiver = new FileReceiver(messageListeners);
        messageReceiver = new MessageReceiver(messageListeners);
        streamReceiver = new StreamReceiver(messageListeners);
    }

    @Override
    public void onPayloadReceived(@NonNull String endpointId,
                                  @NonNull Payload payload) {
        fileReceiver.onPayloadReceived(endpointId, payload);
        messageReceiver.onPayloadReceived(endpointId, payload);
        streamReceiver.onPayloadReceived(endpointId, payload);
    }

    @Override
    public void onPayloadTransferUpdate(@NonNull String endpointId,
                                        @NonNull PayloadTransferUpdate payloadTransferUpdate) {
        fileReceiver.onPayloadTransferUpdate(endpointId, payloadTransferUpdate);
        messageReceiver.onPayloadTransferUpdate(endpointId, payloadTransferUpdate);
        streamReceiver.onPayloadTransferUpdate(endpointId, payloadTransferUpdate);
    }

    public void setDistributionService(@NonNull IMessageDistributionService distributionService) {
        messageReceiver.setDistributionService(distributionService);
    }

    public void unsetDistributionService() {
        messageReceiver.unsetDistributionService();
    }
}
