package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility;

public interface MessageFactory {
    String fabricateMessage(String... message);
    void transferFabricatedMessage(String message);
}
