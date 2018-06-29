package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LongSparseArray;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Messages.JsonFileDataMessage;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

public class FileReceiver extends PayloadCallback {
    private final Iterable<OnMessageListener> messageListeners;
    private final LongSparseArray<Payload> files = new LongSparseArray<>(); // [FileId, FilePayload]
    private final LongSparseArray<Long> fileToFileDataAssociations = new LongSparseArray<>(); // [FileId, FileDataId]
    private final LongSparseArray<JsonFileDataMessage> fileDataMessages = new LongSparseArray<>(); // [FileDataId, FileData]

    FileReceiver(Iterable<OnMessageListener> messageListeners) {
        this.messageListeners = messageListeners;
        // TODO: Bind and use MessageDistributionService for fileName messages.
    }

    @Override
    public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
        long id = payload.getId();
        if (payload.getType() == Payload.Type.FILE) {
            files.put(id, payload);
        }
    }

    @Override
    public void onPayloadTransferUpdate(@NonNull String payloadId,
                                        @NonNull PayloadTransferUpdate payloadTransferUpdate) {
        Long filePayloadId = getFilePayloadId(payloadId);
        if (filePayloadId == null) {
            return;
        }
        if (!hasAllInformationFor(filePayloadId)) {
            return;
        }
        if (payloadTransferUpdate.getStatus() != PayloadTransferUpdate.Status.SUCCESS) {
            return;
        }
        Payload filePayload = files.get(filePayloadId);
        long fileDataId = fileToFileDataAssociations.get(filePayloadId);
        JsonFileDataMessage fileDataMessage = fileDataMessages.get(fileDataId);
        clearLists(filePayloadId, fileDataId);
        ParcelFileDescriptor fileDescriptor = getParcelFileDescriptor(filePayload);
        if (fileDescriptor == null) {
            return;
        }
        onFinishedFileTransfer(fileDataMessage, fileDescriptor);
    }

    private void onFinishedFileTransfer(JsonFileDataMessage fileDataMessage,
                                        ParcelFileDescriptor fileDescriptor) {
        for (OnMessageListener listener :
                messageListeners) {
            listener.onFileReceived(fileDescriptor, fileDataMessage.getFileName());
        }
    }

    @Nullable
    private ParcelFileDescriptor getParcelFileDescriptor(Payload filePayload) {
        Payload.File file = filePayload.asFile();
        if (file == null) {
            return null;
        }
        return file.asParcelFileDescriptor();
    }

    private void clearLists(long filePayloadId, long fileDataId) {
        files.delete(filePayloadId);
        fileToFileDataAssociations.delete(filePayloadId);
        fileDataMessages.delete(fileDataId);
    }

    private boolean hasAllInformationFor(long filePayloadId) {
        return files.indexOfKey(filePayloadId) >= 0
                && fileToFileDataAssociations.indexOfKey(filePayloadId) >= 0;
    }

    private Long getFilePayloadId(String payloadId) {
        long payloadIdAsLong = Long.valueOf(payloadId);
        if (files.indexOfKey(payloadIdAsLong) >= 0) {
            return payloadIdAsLong;
        }
        if (fileDataMessages.indexOfKey(payloadIdAsLong) >= 0) {
            JsonFileDataMessage fileDataMessage = fileDataMessages.get(payloadIdAsLong);
            return fileDataMessage.getPayloadId();
        }
        return null;
    }
}
