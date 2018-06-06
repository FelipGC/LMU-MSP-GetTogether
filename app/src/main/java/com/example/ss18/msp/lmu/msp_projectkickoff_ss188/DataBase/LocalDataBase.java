package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

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

    private static String userName = "Unknown user";
    private static Bitmap profilePicture = null;
    /**
     * Stores payloads we sent during an active session (gets cleared after app exits/closes)
     */
    public final static HashMap<Long, Payload> sentPayLoadData = new HashMap<Long, Payload>();

    //Getter & Setter

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userNameNew) {
        userName = userNameNew;
    }

    public static Bitmap getProfilePicture() {
        return profilePicture;
    }

    public static void setProfilePicture(Bitmap profilePicture) {
        LocalDataBase.profilePicture = profilePicture;
    }

    public static String getProfilePictureAsString() {
        profilePicture = getProfilePicture();
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
    public static Bitmap getProfilePictureAsBitmap(String encodedString) {
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
