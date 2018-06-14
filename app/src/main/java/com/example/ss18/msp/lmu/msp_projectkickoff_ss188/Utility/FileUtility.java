package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;

import java.io.File;
import java.io.IOException;

public class FileUtility {

    private static final String TAG = "FileUtility";
    private static final String STORAGE_PATH = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
    /**
     *Renames and saves the payload to a specific location
     */
    public static void renameFile(String newFilename, File file){
        // Rename the file.
        boolean successful = file.renameTo(new File(file.getParentFile(), newFilename));
        Log.i(TAG,"RENAME SUCCESSFUL: " + successful);
        Log.i(TAG,"FILE LOCATION: " + file.getAbsolutePath());
    }
}
