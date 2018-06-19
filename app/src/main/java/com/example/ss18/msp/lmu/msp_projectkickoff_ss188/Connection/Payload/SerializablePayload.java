package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Payload;

import com.google.android.gms.nearby.connection.Payload;

import java.io.Serializable;

public class SerializablePayload implements Serializable {
    final Payload p;
    public SerializablePayload(Payload p){
        this.p = p;
    }
}