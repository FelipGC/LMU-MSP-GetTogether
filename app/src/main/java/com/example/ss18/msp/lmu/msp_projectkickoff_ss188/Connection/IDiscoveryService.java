package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;

public interface IDiscoveryService extends IService {
    void listenDiscovery(EndpointDiscoveryCallback callback);

    void requestConnection(ConnectionEndpoint endpoint);

    Iterable<ConnectionEndpoint> getDiscoveredEndpoints();
}
