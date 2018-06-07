package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;

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

    private Bitmap profilePicture;

    private final static String TAG = "ConnectionEndpoint";

    public ConnectionEndpoint(@NonNull String id, @NonNull String nameAndBitmap) {
        Log.i(TAG,"ConnectionEndpoint created with ID="+id);
        this.id = id;
        //Since we can`t pass a bitmap (= profile picture) directly via the connection process
        //and since we actually want to pass a profile picture before establishing a connection,
        //we pass the serialized bitmap inside the user name, so me must extract it from it and
        // separate it from the user name.
        //The format for nameAndBitmap is = USERNAME : BITMAP
        Bitmap image = extractBitMap(nameAndBitmap);
        setProfilePicture(image);
        this.originalName = this.name = extractName(nameAndBitmap);
        checkForDuplicatedNames();
    }

    /**
     * Extracts the bitmap (profile picture) from the passed string
     *
     * @return bitmap
     */
    private String extractName(String nameAndBitmap) {
        //Last indexOf since the user could use ':' in their username
        int substringDividerIndex = nameAndBitmap.lastIndexOf(':');
        String name = nameAndBitmap.substring(0, substringDividerIndex);
        Log.i(TAG,"extractName() = " + name);
        return name;
    }

    /**
     * Extracts the bitmap from the passed string
     * @return BitMap
     */
    private Bitmap extractBitMap(final String nameAndBitmap) {
        //Last index of since the user could use ':' in their username
        int substringDividerIndex = nameAndBitmap.lastIndexOf(':');
        String bitmapString = nameAndBitmap.substring(substringDividerIndex + 1);
        Log.i(TAG,"extractBitMap() = " + bitmapString);
        if(bitmapString.equals("NO_PROFILE_PICTURE"))
            return null;
        try {
            byte [] encodeByte= Base64.decode(bitmapString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
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

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }
}
