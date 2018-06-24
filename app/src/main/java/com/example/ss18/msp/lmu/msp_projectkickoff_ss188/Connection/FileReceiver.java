package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LongSparseArray;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class FileReceiver extends PayloadCallback {
    private final String TAG = "FileReceiver";
    private final Iterable<OnMessageListener> messageListeners;
    private final LongSparseArray<Payload> incomingFiles = new LongSparseArray<>();
    private final LongSparseArray<String> fileNames = new LongSparseArray<>();
    private final MessageReceiver messageReceiver;

    FileReceiver(Iterable<OnMessageListener> messageListeners) {
        this.messageListeners = messageListeners;
        Collection<OnMessageListener> listeners = new ArrayList<>();
        listeners.add(new OnMessageListener() {
            @Override
            public void onStreamReceived(ParcelFileDescriptor fileDescriptor) {
            }

            @Override
            public void onMessage(String message) {
                try {
                    JSONObject json = new JSONObject(message);
                    String type = json.getString("type");
                    if (!type.equals("fileData")) {
                        return;
                    }
                    Long payloadId = json.getLong("payloadId");
                    String fileName = json.getString("fileName");
                    fileNames.put(payloadId, fileName);
                } catch (JSONException e) {
                    Log.w(TAG, e.getMessage());
                }
            }

            @Override
            public void onFileReceived(ParcelFileDescriptor fileDescriptor, String filename) {
            }
        });
        this.messageReceiver = new MessageReceiver(listeners);
    }

    @Override
    public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
        long id = payload.getId();
        if (payload.getType() == Payload.Type.FILE) {
            incomingFiles.put(id, payload);
        }
        messageReceiver.onPayloadReceived(endpointId, payload);
    }

    @Override
    public void onPayloadTransferUpdate(@NonNull String payloadId,
                                        @NonNull PayloadTransferUpdate payloadTransferUpdate) {
        Long payloadIdAsLong = Long.valueOf(payloadId);
        if (incomingFiles.indexOfKey(payloadIdAsLong) < 0) {
            return;
        }
        if (payloadTransferUpdate.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
            Payload payload = incomingFiles.get(payloadIdAsLong);
            incomingFiles.delete(payloadIdAsLong);
            String fileName = fileNames.get(payloadIdAsLong);
            fileNames.delete(payloadIdAsLong);
            Payload.File file = payload.asFile();
            if (file == null) {
                return;
            }
            ParcelFileDescriptor fileDescriptor = file.asParcelFileDescriptor();
            for (OnMessageListener listener :
                    messageListeners) {
                listener.onFileReceived(fileDescriptor, fileName);
            }
        }
    }
}
