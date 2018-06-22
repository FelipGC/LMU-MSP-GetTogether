package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.AppLogicActivity;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionManager;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;
import com.google.android.gms.nearby.connection.Payload;

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
    private static Uri profilePicture = null;
    private static Bitmap userImage;

    /**
     * Stores payloads we sent during an active session (gets cleared after app exits/closes)
     */
    public final static HashMap<Long, Payload> sentPayLoadData = new HashMap<Long, Payload>();

    /**
     * Stores other viewers inside the specific presentation
     */
    public final static HashMap<String, Uri> idToUri = new HashMap<>();

   //Getter & Setter

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userNameNew) {
        Log.i(TAG, "Profile name set!");
        userName = userNameNew;
    }


    public static void setProfilePictureUri(Uri profilePicture) {
        Log.i(TAG,"SETTING OWN USER PROFILE: " + profilePicture);
        LocalDataBase.profilePicture = profilePicture;
    }

    public static void setProfilePictureBitmap (Bitmap image) {
        Log.i(TAG,"SETTING OWN USER PROFILE AS BITMAP: " + profilePicture);
        LocalDataBase.userImage = image;
    }

    public static Uri getProfilePictureUri() {
        Log.i(TAG,"Own profPic: " + profilePicture);
        return profilePicture;
    }

    public static Bitmap getProfilePictureBitmap() {
        Log.i(TAG,"Own profPic as Bitmap: " + userImage);
        return userImage;
    }

    public static Uri getProfilePictureUri(String id) {
        if(!idToUri.containsKey(id)) return null;
        return idToUri.get(id);
    }

    public static void addUriToID(Uri uri, String id){
        Log.i(TAG,"Added uri: " + uri + " to id: " + id);
        idToUri.put(id, uri);
        AppLogicActivity appLogicActivity = ConnectionManager.getAppLogicActivity();
        if(AppLogicActivity.getUserRole().getRoleType() == User.UserRole.PRESENTER)
            appLogicActivity.getSelectParticipantsFragment().updateParticipantsAvatar();
        else appLogicActivity.getSelectPresenterFragment().updateJoinedPresentersAvatar();
    }
}
