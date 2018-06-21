package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.LocalDataBase;

import org.json.JSONException;
import org.json.JSONObject;

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
       // this.isPresenter = isPresenter;
        checkForDuplicatedNames();
    }

    /**
     * Checks if the device name (NOT the id) is already occupied locally! If so, rename it.
     */
    private void checkForDuplicatedNames() {
        int nrDuplicates = 0;
        for (ConnectionEndpoint otherEndpoint : AppLogicActivity.getInstance().getmService().getConnectedEndpoints()) {
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

    //public boolean isPresenter(){return isPresenter;}

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

    public String toJsonString() {
        try {
            JSONObject json = new JSONObject();
            json.put("displayName", getName());
            json.put("id", getId());
            return json.toString();
        } catch (JSONException ex) {
            Log.w(TAG, ex.getMessage());
            return null;
        }
    }

    public Uri getProfilePicture() {
        Uri uri = LocalDataBase.getProfilePictureUri(id);
        Log.i(TAG,"getProfilePicture() for: " + getName() + " result:\n" + uri);
        return uri;
    }

    public static ConnectionEndpoint parseJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            String displayName = json.getString("displayName");
            String id = json.getString("id");
            return new ConnectionEndpoint(id, displayName);
        } catch(JSONException ex) {
            Log.w(TAG, ex.getMessage());
            return null;
        }
    }
}
