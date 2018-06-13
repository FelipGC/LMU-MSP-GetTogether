package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Iterator;

public interface IDocument {
    Bitmap getPage(int pageNr);
    int getPageCount();
    Iterator<Bitmap> getPages();
    Uri getUri();
    int getActualPageNr();
}
