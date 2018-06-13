package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;

import java.io.File;

public class FileUtility {

    private static final String TAG = "FileUtility";
    private static final String STORAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            + "/GetTogether/";
    /**
     *Renames and saves the payload to a specific location
     */
    public static void storePayLoad(ConnectionEndpoint endpoint, String fileName, File file) {

        Log.i(TAG, "storePayLoad()");
        //Create folder to save the date if it does not exist already.
        File f = new File(pathToEndpoint(endpoint), fileName);
        renameFile(file,f);


    }
    /**
     *Renames and saves the payload to a specific location and returns the Uri
     */
    public static Uri storePayLoadUserProfile(String fileName, File file) {

        Log.i(TAG, "storePayLoad()");
        //Create folder to save the date if it does not exist already.
        File f = new File( STORAGE_PATH + "UserProfiles", fileName);
        if(renameFile(file,f)){
            return Uri.fromFile(f);
        }else return Uri.fromFile(file);
    }
    /**
     * Returns the path to the location where the files from a particular endpoint should be stored
     */
    public static String pathToEndpoint(ConnectionEndpoint endpoint){
        return STORAGE_PATH + endpoint.getName();
    }

    private static boolean renameFile(File oldFile, File newFile){
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        //Rename file
        boolean renamedSuccessful = oldFile.renameTo(newFile);
        Log.i(TAG, "renamedSuccessful: " + renamedSuccessful);
        return renamedSuccessful;
    }
}
