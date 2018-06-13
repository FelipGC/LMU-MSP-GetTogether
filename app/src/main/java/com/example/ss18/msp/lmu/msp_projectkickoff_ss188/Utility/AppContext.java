package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility;


import android.content.ContentResolver;

public interface AppContext {
    void displayShortMessage(String message);
    String getString(int id);
    ContentResolver getContentResolver();
}
