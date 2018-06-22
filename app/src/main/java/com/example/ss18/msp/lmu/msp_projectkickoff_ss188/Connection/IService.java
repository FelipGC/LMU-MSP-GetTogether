package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.os.ParcelFileDescriptor;

import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;

interface IService {
    void listenLifecycle(ConnectionLifecycleCallback connectionLifecycleCallback);

    void disconnectFromUser(String id);

    void broadcastMessage(String message);
    void sendMessageTo(String id,String message);
    void broadcastStream(ParcelFileDescriptor fileDescriptor);
    void broadcastFile(ParcelFileDescriptor fileDescriptor);
    void sendFile(String endpointId, ParcelFileDescriptor fileDescriptor);

    Iterable<ConnectionEndpoint> getConnectedEndpoints();

    Iterable<ConnectionEndpoint> getPendingEndpoints();
}
