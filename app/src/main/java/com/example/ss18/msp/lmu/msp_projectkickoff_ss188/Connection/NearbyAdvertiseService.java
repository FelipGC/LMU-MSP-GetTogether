package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.SystemMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class NearbyAdvertiseService extends AbstractConnectionService implements IAdvertiseService {
    private final String TAG = "AdvertiseService";
    private final IBinder binder = new NearbyAdvertiseBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void startService() {
        AdvertisingOptions.Builder builder = new AdvertisingOptions.Builder();
        builder.setStrategy(STRATEGY);
        connectionsClient.startAdvertising(AppLogicActivity.getUserRole().getUserName(),
                serviceID, connectionLifecycleCallback,
                builder.build()).addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        // We're advertising!
                        Log.i(TAG, "We are advertising...");
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // We were unable to start advertising.
                                Log.i(TAG, "Something went wrong!");
                                e.printStackTrace();
                            }
                        });
    }

    @Override
    protected ConnectionLifecycleCallback initLifecycle() {return null;}

    @Override
    public void stopService() {
        connectionsClient.stopAdvertising();
    }

    @Override
    public void acceptRequest(String endpointId) {
        if (alreadyConnected(endpointId)) {
            return;
        }
        if (isNotPending(endpointId)) {
            return;
        }
        connectionsClient.acceptConnection(endpointId, payloadReceiver);
    }

    @Override
    public void rejectRequest(String endpointId) {
        if (alreadyConnected(endpointId)) {
            return;
        }
        if (isNotPending(endpointId)) {
            return;
        }
        connectionsClient.rejectConnection(endpointId);
    }

    public class NearbyAdvertiseBinder extends Binder implements IServiceBinder {
        public IAdvertiseService getService() {
            return NearbyAdvertiseService.this;
        }
    }

    public String getNameOfEndpoint(String endpointId){
        if(connectedEndpoints.containsKey(endpointId))
            return connectedEndpoints.get(endpointId).getName();
        if(shortlyDisconnected.containsKey(endpointId)) {
            String name = shortlyDisconnected.get(endpointId);
            shortlyDisconnected.remove(endpointId);
            return name;
        }
        return null;
    }
}
