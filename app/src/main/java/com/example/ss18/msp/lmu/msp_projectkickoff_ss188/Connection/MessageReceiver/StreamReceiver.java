package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public class StreamReceiver extends PayloadCallback {

    private final Iterable<IOnMessageListener> messageListeners;

    StreamReceiver(Iterable<IOnMessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    @Override
    public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
        if (payload.getType() != Payload.Type.STREAM) {
            return;
        }
        for (IOnMessageListener listener :
                messageListeners) {
            Payload.Stream stream = payload.asStream();
            if (stream == null) {
                return;
            }
            ParcelFileDescriptor fileDescriptor = stream.asParcelFileDescriptor();
            listener.onStreamReceived(fileDescriptor);
        }
    }

    @Override
    public void onPayloadTransferUpdate(@NonNull String endpointId,
                                        @NonNull PayloadTransferUpdate payloadTransferUpdate) {
    }
}
