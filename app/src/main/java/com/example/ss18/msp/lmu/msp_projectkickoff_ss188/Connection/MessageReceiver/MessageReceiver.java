package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.IMessageDistributionService;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.UnsupportedEncodingException;

public class MessageReceiver extends PayloadCallback {
    private final Iterable<IOnMessageListener> messageListeners;
    private IMessageDistributionService messageDistributionService;

    MessageReceiver(Iterable<IOnMessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    @Override
    public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
        if (payload.getType() != Payload.Type.BYTES) {
            return;
        }
        byte[] messageData = payload.asBytes();
        if (messageData == null) {
            return;
        }
        try {
            String message = new String(messageData, "UTF-8");
            for (IOnMessageListener listener :
                    messageListeners) {
                listener.onMessage(message);
            }
            if (messageDistributionService != null) {
                messageDistributionService.put(message);
            }
        } catch (UnsupportedEncodingException ex) {
            Log.w("MessageReceiver", ex.getMessage());
        }
    }

    @Override
    public void onPayloadTransferUpdate(@NonNull String s,
                                        @NonNull PayloadTransferUpdate payloadTransferUpdate) {
    }

    public void setDistributionService(@NonNull IMessageDistributionService distributionService) {
        messageDistributionService = distributionService;
    }

    public void unsetDistributionService() {
        messageDistributionService = null;
    }
}
