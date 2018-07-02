package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.os.ParcelFileDescriptor;

import java.io.File;

public interface IOnMessageListener {
    void onStreamReceived(ParcelFileDescriptor fileDescriptor);

    void onMessage(String message);

    void onFileReceived(File file, String filename);
}
