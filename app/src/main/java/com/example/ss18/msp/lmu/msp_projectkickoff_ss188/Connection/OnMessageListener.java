package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.os.ParcelFileDescriptor;

public interface OnMessageListener {
    void onStreamReceived(ParcelFileDescriptor fileDescriptor);

    void onMessage(String message);

    void onFileReceived(ParcelFileDescriptor fileDescriptor, String filename);
}
