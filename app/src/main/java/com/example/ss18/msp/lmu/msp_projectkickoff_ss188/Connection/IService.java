package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.os.ParcelFileDescriptor;

import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;

import java.util.List;

interface IService {
    void listenLifecycle(ConnectionLifecycleCallback connectionLifecycleCallback);

    void listenMessage(OnMessageListener messageListener);

    void broadcastMessage(String message);

    void broadcastStream(ParcelFileDescriptor fileDescriptor);

    void broadcastFile(ParcelFileDescriptor fileDescriptor, String fileName);

    void sendMessage(String endpointId, String message);

    void sendFile(String endpointId, ParcelFileDescriptor fileDescriptor, String fileName);

    List<ConnectionEndpoint> getConnectedEndpoints();

    List<ConnectionEndpoint> getPendingEndpoints();

    void startService();
    void stopService();
}
