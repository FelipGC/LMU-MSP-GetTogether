package de.lmu.msp.gettogether.DataBase;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import de.lmu.msp.gettogether.Activities.AppLogicActivity;
import de.lmu.msp.gettogether.Connection.ConnectionManager;
import de.lmu.msp.gettogether.Users.User;
import com.google.android.gms.nearby.connection.Payload;

import java.util.ArrayList;
import java.util.HashMap;

import de.lmu.msp.gettogether.Activities.AppLogicActivity;
import de.lmu.msp.gettogether.Users.User;

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
    public static final boolean[] connectionSettings = new boolean[4]; // 0 = AutoConnect 1 = Chat anonymisieren  2=Send mising Files 3= sendMissing chat

    public static final ArrayList<Uri> urisSent = new ArrayList<>();
    public static final ArrayList<Payload> chatHistory = new ArrayList<>();

    public static final HashMap<String,String> otherUsersNameToID = new HashMap<>();
    /**
     * Stores other viewers inside the specific presentation
     */
    public final static HashMap<String, Uri> idToUri = new HashMap<>();

    public static void resetDataBaseCache(){
        otherUsersNameToID.clear();
        chatHistory.clear();
        urisSent.clear();
    }
    //Getter & Setter

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userNameNew) {
        Log.i(TAG, "Profile name set!");
        userName = userNameNew;
    }


    public static void setProfilePictureUri(Uri profilePicture) {
        Log.i(TAG, "SETTING OWN USER PROFILE: " + profilePicture);
        LocalDataBase.profilePicture = profilePicture;
    }

    public static void setProfilePictureBitmap(Bitmap image) {
        Log.i(TAG, "SETTING OWN USER PROFILE AS BITMAP: " + profilePicture);
        LocalDataBase.userImage = image;
    }

    public static Uri getProfilePictureUri() {
        Log.i(TAG, "Own profPic: " + profilePicture);
        return profilePicture;
    }

    public static Bitmap getProfilePictureBitmap() {
        Log.i(TAG, "Own profPic as Bitmap: " + userImage);
        return userImage;
    }

    public static Uri getProfilePictureUri(String id) {
        Log.i(TAG, "getProfilePictureUri() ID: " + id + " " + idToUri.keySet() + " " + idToUri.get(id));
        if (!idToUri.containsKey(id)) return null;
        return idToUri.get(id);
    }

    public static void addUriToID(Uri uri, String id) {
        Log.i(TAG, "Added uri: " + uri + " to id: " + id);
        idToUri.put(id, uri);
        Log.i(TAG, "idToUri: " + idToUri.toString());
        AppLogicActivity appLogicActivity = ConnectionManager.getAppLogicActivity();
        if (AppLogicActivity.getUserRole().getRoleType() == User.UserRole.PRESENTER)
            appLogicActivity.getSelectParticipantsFragment().updateParticipantsAvatar();
        else appLogicActivity.getSelectPresenterFragment().updateJoinedPresentersAvatar();
    }
// 0 = AutoConnect 1 = Chat anonymisieren  2=Send mising Files 3= sendMissing chat
   public static boolean isAutoConnect(){
        return connectionSettings[0];
    }
    public static boolean isChatAnonymized(){
        return connectionSettings[1];
    }
    public static boolean isSendMissingChat(){
        return connectionSettings[2];
    }
    public static boolean isSendMissingFiles(){
        return connectionSettings[3];
    }
}
