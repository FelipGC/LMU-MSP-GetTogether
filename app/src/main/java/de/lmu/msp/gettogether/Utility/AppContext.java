package de.lmu.msp.gettogether.Utility;


import android.content.ContentResolver;

public interface AppContext {
    void displayShortMessage(String message);
    ContentResolver getContentResolver();
}
