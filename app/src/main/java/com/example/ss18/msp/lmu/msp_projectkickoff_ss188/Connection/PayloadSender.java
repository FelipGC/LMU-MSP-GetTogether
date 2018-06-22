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
     * Sends a Payload object out to ALL endPoints
     */
    private void sendPayloadBytes(Payload payload) {
        appLogicActivity.getmService().broadcastMessage(String.valueOf(payload.asBytes()));
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

    public void sendLocation(Location location) {
        String message = "LOCATION:" + location.getLatitude() + "/" + location.getLongitude();
        Payload payload = Payload.fromBytes(message.getBytes());

        sendPayloadBytes(payload);
    }

    public void sendDistance(float distance) {
        String message = "DISTANCE:" + distance;
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
    public void sendPokeMessage() {
        // Adding the POKE_S tag to identify start vibration messages on receive.
        String messageToSend = "POKE:" + "S";
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
        String messageToSend = "POKE:" + "E";
        Log.i(TAG, "sendStopPokingMessage()");
        // Send the name of the payload/file as a bytes payload first!
        try {
            Payload payload = Payload.fromBytes(messageToSend.getBytes("UTF-8"));
            sendPayloadBytes(payload);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void startSendingVoice(ParcelFileDescriptor pfD){
        sendPayloadStream(pfD);
    }
}
