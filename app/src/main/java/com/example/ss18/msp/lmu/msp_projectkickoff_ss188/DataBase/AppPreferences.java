package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class AppPreferences {

    private static AppPreferences instance = null;
    private SharedPreferences preferences;

    private AppPreferences(){
    }
    public static AppPreferences getInstance(Context context){
        if(instance==null){
            instance = new AppPreferences();
            instance.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return instance;
    }

    static final String PREF_USER = "preferences_username";
    static final String PREF_IMAGE = "preferences_image";
    static final String PREF_BITMAP = "preferences_bitmap";

    public String getUsername(){
        String username = preferences.getString(PREF_USER, null);
        return username;
    }

    public Uri getUserImage(){
        String image = preferences.getString(PREF_IMAGE, null);
        if(image == null) return null;
        else{
            Uri uri = Uri.parse(image);
            return uri;
        }
    }

    public Bitmap getUserImageBitmap(){
        String image = preferences.getString(PREF_BITMAP, null);
        if(image == null) return null;
        else{
            try{
                byte [] encodeByte = Base64.decode(image,Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                return bitmap;
            }catch(Exception e){
                e.getMessage();
                return null;
            }
        }
    }

    public boolean setUsername(String username){
        boolean success = saveStringPreference(PREF_USER, username);
        if(success) LocalDataBase.setUserName(username); //TODO dirty!
        return success;
    }

    public boolean setUserImage(String uri){
        boolean success = saveStringPreference(PREF_IMAGE, uri);
        if(success) LocalDataBase.setProfilePictureUri(Uri.parse(uri)); //TODO dirty!
        return success;
    }

    public boolean setUserImageBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String image = Base64.encodeToString(b, Base64.DEFAULT);
        boolean success = saveStringPreference(PREF_BITMAP, image);
        if(success) LocalDataBase.setProfilePictureBitmap(bitmap);
        return success;
    }

    private boolean saveStringPreference(String id, String value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(id,value);
        return editor.commit();
    }
}
