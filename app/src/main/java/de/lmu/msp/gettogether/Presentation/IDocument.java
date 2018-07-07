package de.lmu.msp.gettogether.Presentation;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Iterator;

public interface IDocument {
    Bitmap getPage(int pageNr);
    int getPageCount();
    Uri getUri();
    String getFileName();
}
