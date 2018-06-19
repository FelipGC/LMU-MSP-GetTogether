package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.location.Location;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl.LocationUtility;
import com.google.android.gms.nearby.connection.Payload;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

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

    public void sendPayloadBytesToSpecific(ConnectionEndpoint recipient, Payload payload){
        cM.getConnectionsClient().sendPayload(recipient.getId(),payload);
    }

    /**
     * Sends a Payload object out to ALL endPoints
     */
    private void sendPayloadBytes(Payload payload) {
        for (String endpointId : cM.getEstablishedConnections().keySet()) {
            Log.i(TAG, "sendPayloadBytes to: " + endpointId);
            cM.getConnectionsClient().sendPayload(endpointId, payload);
        }
    }
    /**
     * Sends a Payload stream out to ALL endPoints
     */
    private void sendPayloadStream(Payload payload){
        for (String endpointId : cM.getEstablishedConnections().keySet()) {
            Log.i(TAG, "sendPayloadStream to: " + endpointId);
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
        Log.i(TAG,"PL: " + payload + " Name: " + payloadStoringName);
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

    public void sendLocation(Location location) {
        byte[] b = LocationUtility.getLocationAsBytes(location);
        String message = "LOCATION:" + location.getLongitude() + "/" + location.getLatitude();
        Payload payload = Payload.fromBytes(message.getBytes());

        sendPayloadBytes(payload);
    }

    public void sendDistanceWarning(float distance){
        String message = "DISTANCE:"+distance;
        Payload payload = Payload.fromBytes(message.getBytes());
        sendPayloadBytes(payload);
        //sendPayloadBytesToSpecific(,payload);
    }

   /* private ConnectionEndpoint findPresenter(){
        HashMap<String, ConnectionEndpoint> connections = cM.getEstablishedConnections();
        for (ConnectionEndpoint endpoint : connections.values()) {
            if(endpoint.isPresenter()){
                return endpoint;
            }
        }
        return null;
    }*/

    /**
     * Sends a poke message to the viewers (makes their device vibrate)
     */
    public void sendPokeMessage()  {
        // Adding the POKE_S tag to identify start vibration messages on receive.
        String messageToSend = "POKE:"+"S";
        Log.i(TAG, "sendPokeMessage()");
        // Send the name of the payload/file as a bytes payload first!
        try {
            Payload payload = Payload.fromBytes(messageToSend.getBytes("UTF-8"));
            sendPayloadBytes(payload);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a STOP poke message to the viewers (makes their device STOP vibrating)
     */
    public void sendStopPokingMessage() {
        String messageToSend = "POKE:"+"E";
        Log.i(TAG, "sendStopPokingMessage()");
        // Send the name of the payload/file as a bytes payload first!
        try {
            Payload payload = Payload.fromBytes(messageToSend.getBytes("UTF-8"));
            sendPayloadBytes(payload);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void startSendingVoice(Payload payload){
        sendPayloadStream(payload);
    }
}
