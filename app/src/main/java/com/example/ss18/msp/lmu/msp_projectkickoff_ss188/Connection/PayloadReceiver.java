package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ChatFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.FileUtility;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.FixedSizeList;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.File;
import java.io.UnsupportedEncodingException;

import static com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager.getAppLogicActivity;

public final class PayloadReceiver extends PayloadCallback {

    private final String TAG = "PayloadReceiver";
    private ConnectionManager cM;
    private static final FixedSizeList fixedSizeList = new FixedSizeList();
    //SimpleArrayMap is a more efficient data structure when lots of changes occur (in comparision to hash map)
    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();

    PayloadReceiver(){
        Log.i(TAG,"new PayloadReceiver()");
        cM = ConnectionManager.getInstance();
        Log.i(TAG,"CM: " + cM);
    }

    //Note: onPayloadReceived() is called when the first byte of a Payload is received;
    //it does not indicate that the entire Payload has been received.
    //The completion of the transfer is indicated when onPayloadTransferUpdate() is called with a status of PayloadTransferUpdate.Status.SUCCESS
    @Override
    public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
        //We will be receiving data
        Log.i(TAG, String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
        if (payload.getType() == Payload.Type.BYTES) {
            String payloadFilenameMessage = null;
            try {
                payloadFilenameMessage = new String(payload.asBytes(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //Extracts the payloadId and filename from the message and stores it in the
            //filePayloadFilenames map. The format is payloadId:filename.
            Log.i(TAG, "Received string: " + payloadFilenameMessage);
            try {
                int substringDividerIndex = payloadFilenameMessage.indexOf(':');
                String payloadId = payloadFilenameMessage.substring(0, substringDividerIndex);
                String fileContent = payloadFilenameMessage.substring(substringDividerIndex + 1);
                //We must check whether we are receiving a file name (in order to rename a file)
                //or a chat message
                switch (payloadId) {
                    case "C_ENDPOINT":
                        Log.i(TAG, "Received C_ENDPOINT" + fileContent);
                        substringDividerIndex = fileContent.indexOf(':');
                        String newEndpointID = fileContent.substring(0, substringDividerIndex);
                        String newEndpointName = fileContent.substring(substringDividerIndex + 1);
                        cM.getDiscoveredEndpoints().put(newEndpointID, new ConnectionEndpoint(endpointId, newEndpointName));
                        break;
                    case "CHAT":
                        //If we already received it quit
                        if(fixedSizeList.contains(payload.getId()))
                            return;

                        fixedSizeList.add(payload.getId());
                        //We have a new chat message
                        Log.i(TAG, "Received CHAT MESSAGES" + fileContent);
                        onChatMessageReceived(endpointId, fileContent);
                        //Broadcast chat message to all if presenter
                        if(AppLogicActivity.getUserRole().getRoleType() == User.UserRole.PRESENTER)
                            cM.payloadSender.sendPayloadBytesBut(endpointId,payload);
                        break;
                    default:
                        Log.i(TAG, "Received FILE-NAME: " + fileContent);
                        filePayloadFilenames.put(Long.valueOf(payloadId), fileContent);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (payload.getType() == Payload.Type.FILE) {
            Log.i(TAG, "Received FILE: ID=" + payload.getId());
            // Add this to our tracking map, so that we can retrieve the payload later.
            incomingPayloads.put(payload.getId(), payload);
            //TODO: Sending files may take some time. Display progressbar or something
        }
    }

    @Override
    public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
        if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
            //Data fully received.
            Log.i(TAG, "Payload data fully received! ID=" + endpointId);
            //Display a notification.
            //Checks to see if the message is a chat message or a document
            Payload payload = incomingPayloads.remove(update.getPayloadId());
            Log.i(TAG, "onPayloadTransferUpdate()\n" + payload +
                    "\n" + incomingPayloads.toString() +
                    "\n" + filePayloadFilenames.toString());

            if (payload != null) {
                //Display a notification.
                NotificationUtility.displayNotification("Document received",
                        String.format("%s has sent you a document...", cM.getEstablishedConnections().get(endpointId).getName()),
                        NotificationCompat.PRIORITY_DEFAULT);
                //Load data
                if (payload.getType() == Payload.Type.FILE) {
                    // Retrieve the filename and corresponding payload.
                    File payloadFile = payload.asFile().asJavaFile();
                    String fileName = filePayloadFilenames.remove(update.getPayloadId());

                    if (fileName.contains("PROF_PIC")) {
                        int substringDividerIndex = fileName.indexOf(':');
                        Log.i(TAG, "Name to trim: " + fileName);
                        String payLoadTag = fileName.substring(0, substringDividerIndex);
                        String bitMapSender = fileName.substring(substringDividerIndex + 1);
                        //Store image
                        Log.i(TAG, "Trying to store: " + fileName);
                        //TODO: Move and rename file to something good (NOT WORKING?)
                        Uri uriToPic = FileUtility.storePayLoadUserProfile(fileName, payloadFile);
                        Log.i(TAG, "CONTENT " + uriToPic);
                        //Add to local DataBase
                        if (bitMapSender.length() == 0)
                            bitMapSender = endpointId;
                        //Store bitmap
                        LocalDataBase.idToUri.put(bitMapSender, uriToPic);
                        switch (payLoadTag) {
                            case "PROF_PIC_V":
                                Log.i(TAG, "PROF_PIC_V");
                                try {
                                    //Send bitmap to all other endpoints
                                    for (String id :  cM.getEstablishedConnections().keySet()) {
                                        cM.payloadSender.sendPayloadFile(id, payload, payload.getId() + ":PROF_PIC:" + bitMapSender + ":");
                                    }
                                    //Send all other endpoint`s bitmap to the endpoint
                                    for (String id :  cM.getEstablishedConnections().keySet()) {
                                        Uri uri = LocalDataBase.getProfilePictureUri(id);
                                        if (uri == null)
                                            break;
                                        ParcelFileDescriptor file = getAppLogicActivity().getContentResolver().openFileDescriptor(uri, "r");
                                        Payload profilePic = Payload.fromFile(file);
                                        cM.payloadSender.sendPayloadFile(endpointId, profilePic, payload.getId() + ":PROF_PIC:" + id + ":");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case "PROF_PIC":
                                Log.i(TAG, "PROF_PIC");
                                //Send own bitmap to endpoint
                                LocalDataBase.idToUri.put(bitMapSender, Uri.fromFile(payloadFile));
                                break;
                        }
                    } else {
                        Log.i(TAG, "Payload file name: " + payloadFile.getName());
                        ConnectionEndpoint connectionEndpoint = cM.getDiscoveredEndpoints().get(endpointId);
                        //Update inbox-fragment.
                        getAppLogicActivity().getInboxFragment().storePayLoad(connectionEndpoint, fileName, payloadFile);
                    }
                }
            } else Log.i(TAG, "Payload NULL!");

        } else if (update.getStatus() == PayloadTransferUpdate.Status.FAILURE) {
            Log.i(TAG, "Payload status: PayloadTransferUpdate.Status.FAILURE");
        }
    }
    /*
     * Sends the received message from the endpoint to the device
     */

    private void onChatMessageReceived(String id, String message) {
        //Display notification
        NotificationUtility.displayNotificationChat("Chat message received",
                String.format("%s has sent you a message...",cM.getDiscoveredEndpoints().get(id).getName()),
                NotificationCompat.PRIORITY_DEFAULT);
        ChatFragment chat = getAppLogicActivity().getChatFragment();
        chat.getDataFromEndPoint(id, message);
    }

}
