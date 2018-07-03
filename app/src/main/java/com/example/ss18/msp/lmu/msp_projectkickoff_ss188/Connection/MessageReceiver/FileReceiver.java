package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LongSparseArray;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Messages.JsonFileDataMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.File;
import java.io.UnsupportedEncodingException;

public class FileReceiver extends PayloadCallback {
    private final String TAG = "FileReceiver";
    private final Iterable<IOnMessageListener> messageListeners;
    private final LongSparseArray<Payload> files =
            new LongSparseArray<>(); // [FileId, FilePayload]
    private final LongSparseArray<Long> fileToFileDataAssociations =
            new LongSparseArray<>(); // [FileId, FileDataId]
    private final LongSparseArray<JsonFileDataMessage> fileDataMessages =
            new LongSparseArray<>(); // [FileDataId, FileData]


    FileReceiver(Iterable<IOnMessageListener> messageListeners) {
        this.messageListeners = messageListeners;
    }

    @Override
    public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
        long id = payload.getId();
        switch (payload.getType()) {
            case Payload.Type.FILE:
                files.put(id, payload);
                break;
            case Payload.Type.BYTES:
                JsonFileDataMessage fileDataMessage = parseFileDataMessage(payload);
                if (fileDataMessage == null) {
                    return;
                }
                addFileData(id, fileDataMessage);
                break;
        }
    }

    private void addFileData(long id, JsonFileDataMessage fileDataMessage) {
        long fileId = fileDataMessage.getFileId();
        fileDataMessages.put(id, fileDataMessage);
        fileToFileDataAssociations.put(fileId, id);
    }

    @Nullable
    private JsonFileDataMessage parseFileDataMessage(@NonNull Payload payload) {
        try {
            byte[] bytes = payload.asBytes();
            if (bytes == null) {
                return null;
            }
            String json = new String(bytes, "UTF-8");
            BaseMessage message = BaseMessage.fromJsonString(json);
            if (message == null) {
                return null;
            }
            if (!(message instanceof JsonFileDataMessage)) {
                return null;
            }
            return (JsonFileDataMessage) message;
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public void onPayloadTransferUpdate(@NonNull String endpointId,
                                        @NonNull PayloadTransferUpdate payloadTransferUpdate) {
        long payloadId = payloadTransferUpdate.getPayloadId();
        Long fileId = getFileId(payloadId);
        if (fileId == null) {
            return;
        }
        if (missingInformationFor(fileId)) {
            return;
        }
        if (payloadTransferUpdate.getStatus() != PayloadTransferUpdate.Status.SUCCESS) {
            return;
        }
        Payload filePayload = files.get(fileId);
        long fileDataId = fileToFileDataAssociations.get(fileId);
        JsonFileDataMessage fileDataMessage = fileDataMessages.get(fileDataId);
        clearLists(fileId, fileDataId);
        File file = getFileFrom(filePayload);
        if (file == null) {
            return;
        }
        onFinishedFileTransfer(fileDataMessage, file);
    }

    private void onFinishedFileTransfer(JsonFileDataMessage fileDataMessage, File file) {
        String fileName = fileDataMessage.getFileName();
        for (IOnMessageListener listener :
                messageListeners) {
            listener.onFileReceived(file, fileName);
        }
    }

    @Nullable
    private File getFileFrom(Payload filePayload) {
        Payload.File file = filePayload.asFile();
        if (file == null) {
            return null;
        }
        return file.asJavaFile();
    }

    private void clearLists(long filePayloadId, long fileDataId) {
        files.delete(filePayloadId);
        fileToFileDataAssociations.delete(filePayloadId);
        fileDataMessages.delete(fileDataId);
    }

    private boolean missingInformationFor(long fileId) {
        return !(files.indexOfKey(fileId) >= 0
                && fileToFileDataAssociations.indexOfKey(fileId) >= 0);
    }

    private Long getFileId(long payloadId) {
        if (files.indexOfKey(payloadId) >= 0) {
            return payloadId;
        }
        if (fileDataMessages.indexOfKey(payloadId) >= 0) {
            JsonFileDataMessage fileDataMessage = fileDataMessages.get(payloadId);
            return fileDataMessage.getFileId();
        }
        return null;
    }
}
