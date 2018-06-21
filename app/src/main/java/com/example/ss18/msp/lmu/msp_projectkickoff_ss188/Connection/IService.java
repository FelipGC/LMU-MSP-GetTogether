package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;

interface IService {
    void listenLifecycle(ConnectionLifecycleCallback connectionLifecycleCallback);

    void sendMessage(String message);

    Iterable<ConnectionEndpoint> getConnectedEndpoints();

    Iterable<ConnectionEndpoint> getPendingEndpoints();
}
