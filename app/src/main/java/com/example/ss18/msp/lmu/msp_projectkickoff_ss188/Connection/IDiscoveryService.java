package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;

import java.util.Collection;

public interface IDiscoveryService extends IService {
    void register(EndpointDiscoveryCallback callback);

    void requestConnection(ConnectionEndpoint endpoint);

    Collection<ConnectionEndpoint> getDiscoveredEndpoints();
}
