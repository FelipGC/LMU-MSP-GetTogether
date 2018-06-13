package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.google.android.gms.nearby.connection.Payload;

import java.io.UnsupportedEncodingException;

public class PayloadSender {

    private final ConnectionManager cM;
    private final String TAG = "PayloadSender";

    public PayloadSender() {
        Log.i(TAG,"new PayloadSender()");
        cM = ConnectionManager.getInstance();
        Log.i(TAG,"cM: " + cM);
    }

    public void sendChatMessage(String chatMessage) throws UnsupportedEncodingException {
        //String name = LocalDataBase.getUserName();
        // Adding the CHAT tag to identify chat messages on receive.
        String messageToSend = "CHAT" + ":" + LocalDataBase.getUserName() + ":" + chatMessage;
        Log.i(TAG, "SendDataToEndpoint: " + messageToSend);
        //Send message
        // Send the name of the payload/file as a bytes payload first!
        Payload payload = Payload.fromBytes(messageToSend.getBytes("UTF-8"));
        sendPayloadBytes(payload);
    }

    /**
     * Sends a Payload object out to ALL endPoints but a specific one
     */
    public void sendPayloadBytesBut(String idToExclude, Payload payload) {
        for (String endpointId : cM.getEstablishedConnections().keySet()) {
            Log.i(TAG, "sendPayloadBytes to: " + endpointId);
            if(!endpointId.equals(idToExclude))
                cM.getConnectionsClient().sendPayload(endpointId, payload);
        }
    }
    /**
     * Sends a Payload object out to ALL endPoints
     */
    public void sendPayloadBytes(Payload payload) {
        for (String endpointId : cM.getEstablishedConnections().keySet()) {
            Log.i(TAG, "sendPayloadBytes to: " + endpointId);
            cM.getConnectionsClient().sendPayload(endpointId, payload);
        }
    }

    /**
     * Sends a Payload object out to all endPoints
     */
    public void sendPayloadFile(Payload payload, String payloadStoringName) {
        for (String endpointId : cM.getEstablishedConnections().keySet()) {
            try {
                Log.i(TAG, "sendPayloadFile to: " + endpointId);
                sendPayloadFile(endpointId, payload, payloadStoringName);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a Payload object out to one specific endPoint
     */
    public void sendPayloadFile(String endpointId, Payload payload, String payloadStoringName) throws Exception {
        // Send the name of the payload/file as a bytes payload first!
        cM.getConnectionsClient().sendPayload(
                endpointId, Payload.fromBytes(payloadStoringName.getBytes("UTF-8")));
        if (payload != null) {
            Log.i(TAG, "Sent: " + payload.getId() + " with type: " + payload.getType() + " to: " + endpointId);
            //Send the payload data afterwards!
            cM.getConnectionsClient().sendPayload(endpointId, payload);
            //Add to receivedPayLoadData in our data
            LocalDataBase.sentPayLoadData.put(payload.getId(), payload);
        } else throw new Exception("Payload to send must not be null!");
    }
}
