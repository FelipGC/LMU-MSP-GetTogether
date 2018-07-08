package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver;

import android.os.ParcelFileDescriptor;

import java.io.File;

public abstract class OnMessageListener implements IOnMessageListener {

    @Override
    public void onStreamReceived(ParcelFileDescriptor fileDescriptor) {
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onFileReceived(File file, String filename) {
    }
}
