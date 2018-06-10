package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Activities.SettingsActivity;
import com.google.android.gms.nearby.connection.Payload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
    private static Bitmap profilePictureBitmap = null;

    /**
     * Stores payloads we sent during an active session (gets cleared after app exits/closes)
     */
    public final static HashMap<Long, Payload> sentPayLoadData = new HashMap<Long, Payload>();

    /**
     * Stores other viewers inside the specific presentation
     */
    public final static HashMap<String, Bitmap> idToBitmap = new HashMap<>();
    /**
     * Stores other viewers inside the specific presentation
     */
    public final static HashMap<String, Uri> idToUri = new HashMap<>();
    /**
     * Returns a bitmap from a specific viewer
     */
    public static Bitmap getBitmapFromUser(String endPointID) {
        return idToBitmap.get(endPointID);
    }

    /**
     * Adds the bitmap of user to a specific group with a given id
     */
    public static void addBitmapToUser(String id, Uri path, ContentResolver c) {
        Log.i(TAG,"ADDING USER PICTURE");
        idToBitmap.put(id,getBitMapFromUri(path,c,64));
    }

    //Getter & Setter

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userNameNew) {
        Log.i(TAG, "Profile picture set!");
        userName = userNameNew;
    }

    private static Bitmap getBitMapFromUri(Uri uri, ContentResolver contentResolver,int size){
        Log.i(TAG,"getBitMapFromUri() " + uri);
        if (uri == null || String.valueOf(uri).equals("NO_PROFILE_PICTURE"))
            return null;
        //Getting the Bitmap from Gallery
        Bitmap toEncode = null;
        try {
            toEncode = MediaStore.Images.Media.getBitmap(contentResolver, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG,"toEncode() " + uri);
        //Resize the image/bitmap
        //TODO: Maybe rather corp the image instead of resizing
        toEncode = Bitmap.createScaledBitmap(toEncode, size, size, true);
        //Compress the file so that the JAVA Binder doesn't crash
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toEncode.compress(Bitmap.CompressFormat.PNG, 100, out);
        Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        return bitmap;
    }
    public static void getProfilePictureAsBitMap(final SettingsActivity s) {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                if (profilePicture == null || profilePicture.equals("NO_PROFILE_PICTURE"))
                    return null;
                return getBitMapFromUri(profilePicture,s.getContentResolver(),350);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                s.setProfilePictureBitmap(bitmap);
                super.onPostExecute(bitmap);
            }
        }.execute();
    }

    public static void setProfilePicture(Uri profilePicture) {
        Log.i(TAG,"SETTING OWN USER PROFILE: " + profilePicture.toString());
        LocalDataBase.profilePicture = profilePicture;
    }

    public static String getProfilePictureUri() {
        if(profilePicture == null) return "NO_PROFILE_PICTURE";
        return String.valueOf(profilePicture);
    }
    public static String getProfilePictureUri(String id) {
        if(!idToUri.containsKey(id)) return "NO_PROFILE_PICTURE";
        return String.valueOf(idToUri.get(id));
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

    public static void setProfilePictureBitmap(Bitmap profilePictureBitmap) {
        LocalDataBase.profilePictureBitmap = profilePictureBitmap;
    }

    public static Bitmap getProfilePictureBitmap() {
        return profilePictureBitmap;
    }
}
