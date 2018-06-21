package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.os.ParcelFileDescriptor;

import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;

interface IService {
    void listenLifecycle(ConnectionLifecycleCallback connectionLifecycleCallback);

    void sendMessage(String message);
    void sendStream(ParcelFileDescriptor fileDescriptor);
    void sendFile(ParcelFileDescriptor fileDescriptor);
    void sendFileTo(String endpointId, ParcelFileDescriptor fileDescriptor);

    Iterable<ConnectionEndpoint> getConnectedEndpoints();

    Iterable<ConnectionEndpoint> getPendingEndpoints();
}
