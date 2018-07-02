package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.os.ParcelFileDescriptor;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver.IOnMessageListener;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;

import java.io.File;
import java.util.Collection;

public interface IService {
    void register(ConnectionLifecycleCallback connectionLifecycleCallback);

    void register(IOnMessageListener messageListener);

    void unregister(IOnMessageListener messageListener);

    void broadcastMessage(String message);

    void broadcastStream(ParcelFileDescriptor fileDescriptor);

    void broadcastFile(File file, String fileName);

    void sendMessage(String endpointId, String message);

    void sendFile(String endpointId, File file, String fileName);

    Collection<ConnectionEndpoint> getConnectedEndpoints();

    Collection<ConnectionEndpoint> getPendingEndpoints();

    void disconnect(String endpointId);

    void startService();

    void stopService();
}
