package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

public interface IAdvertiseService extends IService {
    void acceptRequest(String endpointId);

    void rejectRequest(String endpointId);
}
