package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.nearby.connection.Payload;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * A singleton class simulating a data base to store all kinds of necessary information. The data base is
 * stored locally on the android device.
 */
public final class LocalDataBase {

    private LocalDataBase() {
    }

    private static final String TAG = "LocalDataBase";
    private static String userName = "Unknown user";
    private static Bitmap profilePicture = null;
    /**
     * Stores payloads we sent during an active session (gets cleared after app exits/closes)
     */
    public final static HashMap<Long, Payload> sentPayLoadData = new HashMap<Long, Payload>();

    /**
     * Stores other viewers inside the specific presentation
     */
    public final static HashMap<String, Bitmap> idToBitmap = new HashMap<>();

    /**
     * Returns a bitmap from a specific viewer
     */
    public static Bitmap getBitmapFromUser(String endPointID) {
        return idToBitmap.get(endPointID);
    }

    /**
     * Adds the bitmap of user to a specific group with a given id
     */
    public static void addBitmapToUser(String id, Bitmap bitmap){
        Log.i(TAG,"addBitmapToUser() ID="+id + " bitmap: " + bitmap);
        idToBitmap.put(id,bitmap);
    }

    //Getter & Setter

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userNameNew) {
        Log.i(TAG,"Profile picture set!");
        userName = userNameNew;
    }

    public static Bitmap getProfilePicture() {
        return profilePicture;
    }

    public static void setProfilePicture(Bitmap profilePicture) {
        Log.i(TAG,"SETTING OWN USER PROFILE: " + profilePicture.toString());
        LocalDataBase.profilePicture = profilePicture;
    }

    public static String getProfilePictureAsString() {
        profilePicture = getProfilePicture();
        return getProfilePictureAsString(profilePicture);
    }

    public static String getProfilePictureAsString(Bitmap profilePicture) {
        if(profilePicture == null) return "NO_PROFILE_PICTURE";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        profilePicture.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap stringToBitmap(String encodedString) {
        if(encodedString.equals("NO_PROFILE_PICTURE"))
            return null;
        try{
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }
}
