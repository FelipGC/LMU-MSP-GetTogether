package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.content.ContentResolver;
import android.graphics.Bitmap;

public interface IPdfViewer {
    void showPdfPage(Bitmap bitmap);
    void showErrorToast();
    ContentResolver getContentResolver();
}
