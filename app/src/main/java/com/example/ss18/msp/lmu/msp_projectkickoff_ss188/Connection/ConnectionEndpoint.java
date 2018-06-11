package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;

/**
 * Represents a device we can connect to.
 */
public final class ConnectionEndpoint {

    @NonNull
    private final String id; //Should be unique!
    @NonNull
    private String name; //Becomes unique if it wasn`t at instantiation.
    @NonNull
    private final String originalName; //Doesnt need to be unique

    private final static String TAG = "ConnectionEndpoint";

    public ConnectionEndpoint(@NonNull String id, @NonNull String userName) {
        Log.i(TAG,"ConnectionEndpoint created with ID="+id+"\n UserName:\n"+userName);
        this.id = id;
        //Since we can`t pass a bitmap (= profile picture) directly via the connection process
        //and since we actually want to pass a profile picture before establishing a connection,
        //we pass the serialized bitmap inside the user name, so me must extract it from it and
        // separate it from the user name.
        //The format for nameAndBitmap is = USERNAME : BITMAP

        //LocalDataBase.addBitmapToUser(id,extractBitMap(nameAndBitmap));
        this.originalName = this.name = userName;
        checkForDuplicatedNames();
    }

    /**
     * Checks if the device name (NOT the id) is already occupied locally! If so, rename it.
     */
    private void checkForDuplicatedNames() {
        int nrDuplicates = 0;
        for (ConnectionEndpoint otherEndpoint : AppLogicActivity.getConnectionManager().getDiscoveredEndpoints().values()) {
            if (otherEndpoint.getOriginalName().equals(originalName))
                nrDuplicates++;
        }
        if (nrDuplicates > 0)
            name = name + " " + nrDuplicates;
    }

    //Getters
    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getOriginalName() {
        return originalName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ConnectionEndpoint) {
            ConnectionEndpoint other = (ConnectionEndpoint) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("ConnectionEndpoint{id=%s, name=%s}", id, name);
    }

    public Uri getProfilePicture() {
        Uri uri = LocalDataBase.getProfilePictureUri(id);
        Log.i(TAG,"getProfilePicture() for: " + getName() + " result:\n" + uri);
        return uri;
    }
}
