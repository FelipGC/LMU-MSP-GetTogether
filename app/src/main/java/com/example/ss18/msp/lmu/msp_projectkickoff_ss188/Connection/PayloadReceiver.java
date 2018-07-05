package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DistanceControl.CheckDistanceService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.ChatFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Fragments.InboxFragment;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.FixedSizeList;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.NotificationUtility;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager.getAppLogicActivity;
import static com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.Constants.MAX_GPS_DISTANCE;

public final class PayloadReceiver extends PayloadCallback {

    private final String TAG = "PayloadReceiver";
    private static final FixedSizeList fixedSizeList = new FixedSizeList();
    //SimpleArrayMap is a more efficient data structure when lots of changes occur (in comparision to hash map)
    private final SimpleArrayMap<Long, Payload> incomingPayloads = new SimpleArrayMap<>();
    private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();

    private ConnectionManager cM;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (getAppLogicActivity() != null)
                getAppLogicActivity().serviceConnections.remove(this);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionManager.ConnectionManagerBinder myBinder = (ConnectionManager.ConnectionManagerBinder) service;
            cM = myBinder.getService();
        }
    };

    PayloadReceiver() {
        Log.i(TAG, "new PayloadReceiver()");
        Intent intent = new Intent(getAppLogicActivity(), ConnectionManager.class);
        getAppLogicActivity().bindService(intent, mServiceConnection, getAppLogicActivity().BIND_AUTO_CREATE);
        getAppLogicActivity().serviceConnections.add(mServiceConnection);
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
                        Log.i(TAG, "Received C_ENDPOINT: " + fileContent);
                        substringDividerIndex = fileContent.indexOf(':');
                        String newEndpointID = fileContent.substring(0, substringDividerIndex);
                        String newEndpointName = fileContent.substring(substringDividerIndex + 1);
                        LocalDataBase.otherUsersNameToID.put(newEndpointName,newEndpointID);
                        break;
                    case "A_CHAT":
                        //If we already received it quit
                        if (fixedSizeList.contains(payload.getId()))
                            return;
                        Log.i(TAG, "Received A_CHAT: " + fileContent);
                        fixedSizeList.add(payload.getId());
                        //We have a new chat message
                        onChatMessageReceived(endpointId, fileContent, true);
                        //TODO: Anonymize profile picture?
                        break;
                    case "CHAT":
                        //If we already received it quit
                        if (fixedSizeList.contains(payload.getId()))
                            return;
                        Log.i(TAG, "Received CHAT: " + fileContent);
                        fixedSizeList.add(payload.getId());
                        //We have a new chat message
                        onChatMessageReceived(endpointId, fileContent, false);
                        //Broadcast chat message to all if presenter
                        if (AppLogicActivity.getUserRole().getRoleType() == User.UserRole.PRESENTER) {
                            if (LocalDataBase.isChatAnonymized())
                                cM.payloadSender.sendPayloadBytesAnonymizedBut(endpointId, payload);
                            else
                                cM.payloadSender.sendPayloadBytesBut(endpointId, payload);
                            LocalDataBase.chatHistory.add(payload);
                        }
                        break;
                    case "POKE":
                        Log.i(TAG, "Received POKE" + fileContent);
                        if (fileContent.equals("S"))
                            NotificationUtility.startVibration();
                        else
                            NotificationUtility.endVibration();
                        break;
                    case "DISTANCE":
                        Log.i(TAG, "Received DISTANCE " + fileContent);
                        float distance = Float.parseFloat(fileContent);
                        float oldDistance = cM.getEstablishedConnections().get(endpointId).getLastKnownDistance();
                        if (distance > MAX_GPS_DISTANCE
                                && oldDistance < distance) {
                            onDistanceWarningReceived(endpointId, fileContent);
                        }
                        //Update location
                        cM.getEstablishedConnections().get(endpointId).setLastKnownDistance(distance);
                        break;
                    case "LOCATION":
                        String[] coords = fileContent.split("/");
                        float latitude = Float.parseFloat(coords[0]);
                        float longitude = Float.parseFloat(coords[1]);
                        Location location = new Location("unknown");
                        location.setLongitude(longitude);
                        location.setLatitude(latitude);
                        onLocationReceived(location);
                        break;
                    case "NULL_PROF_PIC":
                        Log.i(TAG,"NULL_PROF_PIC received.");
                        sendEndpointsProfilePictureTo(endpointId);
                        break;
                    default:
                        Log.i(TAG, "Received FILE-NAME: " + fileContent + " PayloadID=" + payloadId);
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
        } else if (payload.getType() == Payload.Type.STREAM) {
            Log.i(TAG, "Received STREAM: ID=" + payload.getId());
            //We received a stream. i.e Voice stream
            receivedVoiceStream(payload.asStream().asInputStream());
        }
    }

    @Override
    public void onPayloadTransferUpdate(@NonNull String endpointId, final PayloadTransferUpdate update) {

        if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
            //Data fully received.
            Log.i(TAG, "Payload data fully received! ID=" + endpointId);
            //Checks to see if the message is a chat message or a document
            Payload payload = incomingPayloads.remove(update.getPayloadId());
            Log.i(TAG, "onPayloadTransferUpdate() Incoming payload: " + payload);
            if (payload != null) {
                //Load data
                if (payload.getType() == Payload.Type.FILE) {
                    receivedFileParser(payload, update, endpointId);
                } else Log.i(TAG, "Payload received is not Type.FILE, but: " + payload.getType());
            }
        } else if (update.getStatus() == PayloadTransferUpdate.Status.FAILURE) {
            Log.i(TAG, "Payload status: PayloadTransferUpdate.Status.FAILURE");
        } else {
            Payload p = incomingPayloads.get(update.getPayloadId());
            if (p != null && p.getType() == Payload.Type.FILE)
                NotificationUtility.displayProgressNotifications((int) update.getBytesTransferred(), (int) update.getTotalBytes());
        }
    }
    /*
     * Sends the received message from the endpoint to the device
     */

    private void onChatMessageReceived(String id, String message, boolean anonymous) {
        Log.i(TAG, "RECEIVED CHAT MESSAGES" + message + cM.getDiscoveredEndpoints().get(id).getName());
        String name = cM.getDiscoveredEndpoints().get(id).getName();

        int substringDividerIndex = message.indexOf(':');
        String payloadSender = message.substring(0, substringDividerIndex);
        if (!payloadSender.equals(name)) {
            Log.i(TAG, "payloadsender:" + payloadSender + " other:" + name);
            id = LocalDataBase.otherUsersNameToID.get(payloadSender);
            if (anonymous)
                name = getAppLogicActivity().getResources().getString(R.string.anonymous);
            else
                name = payloadSender;
        }
        //Display notification
        Resources resources = getAppLogicActivity().getResources();
        NotificationUtility.displayNotificationChat(resources.getString(R.string.notif_chat_title),
                resources.getString(R.string.notif_chat_body, name),
                NotificationCompat.PRIORITY_DEFAULT);
        ChatFragment chat = getAppLogicActivity().getChatFragment();
        chat.getDataFromEndPoint(id, message, anonymous);
    }

    private void onLocationReceived(Location receivedLocation) {
        Intent intent = new Intent(getAppLogicActivity(), CheckDistanceService.class);
        intent.putExtra("location", receivedLocation);
        getAppLogicActivity().startService(intent);
    }

    private void onDistanceWarningReceived(String senderId, String distance) {
        String senderName = cM.getDiscoveredEndpoints().get(senderId).getName();
        Resources resources = getAppLogicActivity().getResources();
        NotificationUtility.displayNotification(resources.getString(R.string.distance_warning),
                resources.getString(R.string.notif_distance_body_presenter, senderName, distance),
                NotificationCompat.PRIORITY_DEFAULT);
    }

    /**
     * Implements the actions to execute after fully receiving a document.
     */
    private void receivedFileParser(Payload payload, PayloadTransferUpdate update, String endpointId) {
        // Retrieve the filename and corresponding payload.
        File payloadFile = payload.asFile().asJavaFile();
        String fileName = filePayloadFilenames.remove(update.getPayloadId());
        if (fileName != null) {
            //Did we receive a Profile Picture?
            if (fileName.contains("PROF_PIC")) {
                Log.i(TAG, "Received PROF_PIC");
                profilePictureReceived(fileName, payloadFile, endpointId);
            }
            //Did we receive an Image?
            else if (fileName.contains("IMAGE_PIC:")) {
                Log.i(TAG, "Received IMAGE_PIC");
                receivedImageFully(payloadFile, endpointId);
            }
            //We received a document which is not an image nor an profile picture
            else {
                Log.i(TAG, "Received document file");
                receivedFileFully(payloadFile, endpointId);
            }
        } else {
            Log.i(TAG, "Filename is null...");
        }

    }

    private void profilePictureReceived(String fileName, File payloadFile, String endpointId) {
        Log.i(TAG, "To trim: " + fileName);
        String[] parts = fileName.split(":");
        Log.i(TAG, "Parts: " + parts.toString());
        if(parts.length<1) return;
        String payLoadTag = parts[0];
        String bitMapSender = endpointId;
        if (parts.length >= 2) {
            bitMapSender = parts[1];
        }
        //Toast.makeText(getAppLogicActivity(), fileName, Toast.LENGTH_LONG).show();
        //Store image
        //TODO: Move and rename file to something good (NOT WORKING?)
        //Uri uriToPic = FileUtility.storePayLoadUserProfile(fileName, payloadFile);
        Uri uriToPic = Uri.fromFile(payloadFile);
        Log.i(TAG, "CONTENT URI " + uriToPic);
        //Log.i(TAG, "ORIGINAL URI " + Uri.fromFile(payloadFile));
        Log.i(TAG, "BITMAP SENDER: " + bitMapSender);
        //Store bitmap
        LocalDataBase.addUriToID(uriToPic, bitMapSender);
        switch (payLoadTag) {
            //Presenter received a profile picture
            case "PROF_PIC_V":
                Log.i(TAG, "<<PROF_PIC_V>>");
                try {
                    //Uri uri = LocalDataBase.getProfilePictureUri(bitMapSender);
                    profilePictureToEndpoints(bitMapSender,payloadFile);
                    sendEndpointsProfilePictureTo(bitMapSender);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //Viewer received a profile picture
            case "PROF_PIC":
                Log.i(TAG, "<<PROF_PIC>>");
                break;
        }
    }

    /**Sends a profile picture to all other endpoints     */
    private void profilePictureToEndpoints(String bitMapSender, File payloadFile) throws Exception {
        Log.i(TAG,"profilePictureToEndpoints("+bitMapSender+")");
        //Send bitmap to all other endpoints
        for (String id : cM.getEstablishedConnectionsCloned().keySet()) {
            if (!id.equals(bitMapSender)) {
                Payload payloadToSend = Payload.fromFile(payloadFile);
                cM.payloadSender.sendPayloadFile(id, payloadToSend,
                        payloadToSend.getId() + ":PROF_PIC:" + bitMapSender + ":");
            }
        }
    }
    /**Send all other endpoint`s profile picture to the endpoint*/
    private void sendEndpointsProfilePictureTo(String bitMapSender) throws Exception {
        Log.i(TAG,"sendEndpointsProfilePictureTo("+bitMapSender+")");
        //Send all other endpoint`s bitmap to the endpoint
        for (String id : cM.getEstablishedConnectionsCloned().keySet()) {
            Uri uri = LocalDataBase.getProfilePictureUri(id);
            if (id.equals(bitMapSender) || uri == null)
                continue;
            ParcelFileDescriptor file = getAppLogicActivity().getContentResolver().openFileDescriptor(uri, "r");
            Payload profilePic = Payload.fromFile(file);
            cM.payloadSender.sendPayloadFile(bitMapSender, profilePic, profilePic.getId() + ":PROF_PIC:" + id + ":");
        }
    }
    private void receivedImageFully(File payloadFile, String endpointId) {
        if (cM.getEstablishedConnections().get(endpointId) == null)
            return;
        //TODO: RenameFile
        //Display a notification.
        Resources resources = getAppLogicActivity().getResources();
        NotificationUtility.displayNotification(resources.getString(R.string.notif_picture_title),
                resources.getString(R.string.notif_picture_body, cM.getEstablishedConnections().get(endpointId).getName()),
                NotificationCompat.PRIORITY_DEFAULT);
        Log.i(TAG, "Payload file name: " + payloadFile.getName());
        //ConnectionEndpoint connectionEndpoint = cM.getDiscoveredEndpoints().get(endpointId);
        //Update inbox-fragment.
        Uri uri = Uri.fromFile(payloadFile);
        InboxFragment inboxFragment = getAppLogicActivity().getInboxFragment();
        inboxFragment.addPicture(uri);
    }

    private void receivedFileFully(File payloadFile, String endpointId) {
        //TODO: RenameFile
        //Display a notification.
        Resources resources = getAppLogicActivity().getResources();
        NotificationUtility.displayNotification(resources.getString(R.string.notif_file_title),
                resources.getString(R.string.notif_file_body, cM.getEstablishedConnections().get(endpointId).getName()),
                NotificationCompat.PRIORITY_DEFAULT);
        Log.i(TAG, "Payload file name: " + payloadFile.getName());
    }

    /**
     * Gets called after receiving a voice stream
     */
    private void receivedVoiceStream(InputStream inputStream) {
        //TODO: Handle simultaneous receivedVoiceStream()
        AppLogicActivity.getVoiceTransmission().playAudio(inputStream);
    }

}
