package de.lmu.msp.gettogether.Presentation;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RenderedPdfDocument implements IDocument {
    private final int pageCount;
    private final List<Bitmap> pages;
    private final Uri documentUri;
    private final String fileName;

    private RenderedPdfDocument(PdfRenderer pdfRenderer, Uri documentUri, String fileName) {
        this.documentUri = documentUri;
        this.fileName = fileName;
        pages = new ArrayList<>();
        pageCount = pdfRenderer.getPageCount();
        for(int pageNr = 0; pageNr < pageCount; pageNr++) {
            Bitmap page = initPage(pageNr, pdfRenderer);
            pages.add(pageNr, page);
        }
    }

    private Bitmap initPage(int pageNr, PdfRenderer pdfRenderer) {
        PdfRenderer.Page page = pdfRenderer.openPage(pageNr);
        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();
        return bitmap;
    }

    @Override
    public int getPageCount() {
        return pageCount;
    }

    @Override
    public Uri getUri() {
        return documentUri;
    }

    @Override
    public Bitmap getPage(int pageNr) {
        return pages.get(pageNr);
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    private static String getFileName(Uri uri, ContentResolver contentResolver) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static IDocument load(Uri documentUri, ContentResolver contentResolver) throws IOException {
        ParcelFileDescriptor pdfFd = contentResolver.openFileDescriptor(documentUri, "r");
        if (pdfFd == null) {
            throw new FileNotFoundException(documentUri.toString());
        }
        PdfRenderer pdfRenderer = new PdfRenderer(pdfFd);
        String fileName = getFileName(documentUri, contentResolver);
        IDocument renderedPdfDocument = new RenderedPdfDocument(pdfRenderer, documentUri, fileName);
        pdfRenderer.close();
        pdfFd.close();
        return renderedPdfDocument;
    }
}
