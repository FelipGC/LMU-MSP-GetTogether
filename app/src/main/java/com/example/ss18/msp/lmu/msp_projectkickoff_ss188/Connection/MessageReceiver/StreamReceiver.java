package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public class StreamReceiver extends PayloadCallback {

    private final Iterable<OnMessageListener> messageListeners;

    StreamReceiver(Iterable<OnMessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    @Override
    public void onPayloadReceived(@NonNull String s, @NonNull Payload payload) {
        if (payload.getType() != Payload.Type.STREAM) {
            return;
        }
        for (OnMessageListener listener :
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
    public void onPayloadTransferUpdate(@NonNull String s,
                                        @NonNull PayloadTransferUpdate payloadTransferUpdate) {
    }
}
