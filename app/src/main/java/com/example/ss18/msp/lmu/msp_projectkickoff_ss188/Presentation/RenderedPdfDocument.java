package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RenderedPdfDocument implements IDocument {
    private final int pageCount;
    private final List<Bitmap> pages;
    private final Uri documentUri;
    private int actualPageNr;

    private RenderedPdfDocument(PdfRenderer pdfRenderer, Uri documentUri) {
        this.documentUri = documentUri;
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
    public Iterator<Bitmap> getPages() {
        return pages.iterator();
    }

    @Override
    public Uri getUri() {
        return documentUri;
    }

    @Override
    public int getActualPageNr() {
        return actualPageNr;
    }

    @Override
    public Bitmap getPage(int pageNr) {
        actualPageNr = pageNr;
        return pages.get(pageNr);
    }

    public static IDocument load(Uri documentUri, ContentResolver contentResolver) {
        try {
            ParcelFileDescriptor pdfFd = contentResolver.openFileDescriptor(documentUri, "r");
            if (pdfFd == null) {
                return null;
            }
            PdfRenderer pdfRenderer = new PdfRenderer(pdfFd);
            IDocument renderedPdfDocument = new RenderedPdfDocument(pdfRenderer, documentUri);
            pdfRenderer.close();
            pdfFd.close();
            return renderedPdfDocument;
        }
        catch (Exception e) {
            return null;
        }
    }
}
