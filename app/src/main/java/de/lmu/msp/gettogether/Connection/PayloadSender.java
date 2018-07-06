package de.lmu.msp.gettogether.Connection;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.nearby.connection.Payload;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.lmu.msp.gettogether.DataBase.LocalDataBase;

public class PayloadSender {

    private final String TAG = "PayloadSender";
    private ConnectionManager cM;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG,name +"SERVICE DISCCONECTED");
            if(ConnectionManager.getAppLogicActivity() != null)
                ConnectionManager.getAppLogicActivity().serviceConnections.remove(this);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionManager.ConnectionManagerBinder myBinder = (ConnectionManager.ConnectionManagerBinder) service;
            cM = myBinder.getService();
        }
    };

    public PayloadSender() {
        Log.i(TAG, "new PayloadSender()");
        Intent intent = new Intent(ConnectionManager.getAppLogicActivity(), ConnectionManager.class);
        ConnectionManager.getAppLogicActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        ConnectionManager.getAppLogicActivity().serviceConnections.add(mServiceConnection);
    }

    public void sendChatMessage(String chatMessage) throws UnsupportedEncodingException {
        //String name = LocalDataBase.getUserName();
        // Adding the CHAT tag to identify chat messages on receive.
        String messageToSend = "CHAT" + ":" + LocalDataBase.getUserName() + ":" + chatMessage;
        Log.i(TAG, "SendDataToEndpoint: " + messageToSend);
        //Send message
        // Send the name of the payload/file as a bytes payload first!
        Payload payload = Payload.fromBytes(messageToSend.getBytes("UTF-8"));
        LocalDataBase.chatHistory.add(payload);
        sendPayloadBytes(payload);
    }

    /**
     * Sends a Payload object out to ALL endPoints but a specific one
     */
    public void sendPayloadBytesBut(String idToExclude, Payload payload) {
        byte[] bytes = payload.asBytes();
        if (bytes == null) {
            return;
        }
        for (String endpointId : cM.getEstablishedConnectionsCloned().keySet()) {
            Payload payloadToSend = Payload.fromBytes(bytes);
            if (!endpointId.equals(idToExclude)) {
                Log.i(TAG, "sendPayloadBytes to: " + endpointId);
                cM.getConnectionsClient().sendPayload(endpointId, payloadToSend);
            }
        }
    }

    private Payload anonymizePayload(Payload payload){
        byte[] bytes = payload.asBytes();
        if (bytes == null) {
            return null;
        }
        String message = new String(bytes);
            message = "A_"+message;
            Log.i(TAG,message);
            return Payload.fromBytes(message.getBytes());
    }
    /**
     * Sends a Payload object out to ALL endPoints but a specific one but anonymizes the name
     */
    public void sendPayloadBytesAnonymizedBut(String endpointId, Payload chatPayload) {
        Payload anonymizedPayload = anonymizePayload(chatPayload);
        if (anonymizedPayload == null) {
            return;
        }
        sendPayloadBytesBut(endpointId, anonymizedPayload);
    }

    public void sendPayloadBytesAnonymizedToSpecific(String endpointId, Payload chatPayload) {
        Payload anonymizedPayload = anonymizePayload(chatPayload);
        if (anonymizedPayload == null) {
            return;
        }
        sendPayloadBytesToSpecific(endpointId, anonymizedPayload);
    }

    public void sendPayloadBytesToSpecific(String recipient, Payload payload) {
        cM.getConnectionsClient().sendPayload(recipient, payload);
    }

    /**
     * Sends a Payload object out to ALL endPoints
     */
    private void sendPayloadBytes(final Payload payload) {
        if(cM==null) {
            return;
        }
        byte[] bytes = payload.asBytes();
        if (bytes == null) {
            return;
        }
        for (final String endpointId : cM.getEstablishedConnectionsCloned().keySet()) {
            Payload payloadToSend = Payload.fromBytes(bytes);
            Log.i(TAG, "sendPayloadBytes to: " + endpointId);
            cM.getConnectionsClient().sendPayload(endpointId, payloadToSend);
        }
    }

    /**
     * Sends a Payload stream out to ALL endPoints
     */
    private void sendPayloadStream(final String endpointId, final Payload payload) {
        Log.i(TAG, "sendPayloadStream to: " + endpointId);
        cM.getConnectionsClient().sendPayload(endpointId, payload);
    }

    /**
     * Sends a Payload object out to one specific endPoint
     */
    public void sendPayloadFile(final String endpointId, final Payload payload, String payloadStoringName) throws Exception {
        Log.i(TAG, "PL: " + payload + " Name: " + payloadStoringName);
        // Send the name of the payload/file as a bytes payload first!
        cM.getConnectionsClient().sendPayload(
                endpointId, Payload.fromBytes(payloadStoringName.getBytes("UTF-8")));
        if (payload != null) {
            Log.i(TAG, "Sent: " + payload.getId() + " with type: " + payload.getType() + " to: " + endpointId);
            //Send the payload data afterwards!
            Log.i(TAG,"SENDING FILE");
            cM.getConnectionsClient().sendPayload(endpointId, payload);
        } else throw new Exception("Payload to send must not be null!");
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
    }
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

    public void startSendingVoice(String id, Payload payload) {
        sendPayloadStream(id,payload);
    }

    public void sendMessage(String message) {
        if (cM == null) {
            return;
        }
        try {
            byte[] bytes = message.getBytes("UTF-8");
            Payload payload = Payload.fromBytes(bytes);
            List<String> receivers = new ArrayList<>(cM.getEstablishedConnections().keySet());
            cM.getConnectionsClient().sendPayload(receivers, payload);
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, e.getMessage());
        }
    }
}
