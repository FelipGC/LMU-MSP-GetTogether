package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.location.Location;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl.LocationUtility;
import com.google.android.gms.nearby.connection.Payload;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class PayloadSender {

    private final String TAG = "PayloadSender";
    private final AppLogicActivity appLogicActivity;

    public PayloadSender() {
        Log.i(TAG,"new PayloadSender()");
        appLogicActivity = AppLogicActivity.getInstance();
    }

    /**
     * Sends a Payload stream out to ALL endPoints
     */
    private void sendPayloadStream(ParcelFileDescriptor pfD){
        appLogicActivity.getmService().broadcastStream(pfD);
    }

    /**
     * Sends a Payload object out to all endPoints
     */
    public void sendPayloadFile(Payload payload, String payloadStoringName) {
        Log.i(TAG,"PL: " + payload + " Name: " + payloadStoringName);
        // Send the name of the payload/file as a bytes payload first!
        appLogicActivity.getmService().broadcastMessage(payloadStoringName);
        if (payload != null) {
            Log.i(TAG, "Sent: " + payload.getId() + " with type: " + payload.getType());
            appLogicActivity.getmService().broadcastFile(payload.asFile().asParcelFileDescriptor());
            //Add to receivedPayLoadData in our data
            LocalDataBase.sentPayLoadData.put(payload.getId(), payload);
        }
    }

    /**
     * Sends a Payload object out to one specific endPoint
     */
    public void sendPayloadFile(String endpointId, Payload payload, String payloadStoringName) throws Exception {
        appLogicActivity.getmService().broadcastMessage(payloadStoringName);
        appLogicActivity.getmService().sendFile(endpointId, payload.asFile().asParcelFileDescriptor());

    }

    public void startSendingVoice(ParcelFileDescriptor pfD){
        sendPayloadStream(pfD);
    }
}
