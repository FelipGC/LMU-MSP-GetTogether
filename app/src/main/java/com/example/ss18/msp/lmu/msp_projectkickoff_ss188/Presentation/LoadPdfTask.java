package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

public class LoadPdfTask extends AsyncTask<Uri, Void, Bitmap> {
    private IPdfViewer pdfViewer;

    LoadPdfTask(IPdfViewer pdfViewer) {
        this.pdfViewer = pdfViewer;
    }

    @Override
    protected Bitmap doInBackground(Uri... params) {
        Uri documentUri = params[0];
        PdfRenderer pdfRenderer;
        try {
            ParcelFileDescriptor pdfFd = pdfViewer.getContentResolver()
                    .openFileDescriptor(documentUri, "r");
            if (pdfFd == null) {
                pdfViewer.showErrorToast();
                return null;
            }
            pdfRenderer = new PdfRenderer(pdfFd);
        }
        catch (Exception e) {
            pdfViewer.showErrorToast();
            return null;
        }
        PdfRenderer.Page page = pdfRenderer.openPage(0);
        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap == null) {
            pdfViewer.showErrorToast();
            return;
        }
        pdfViewer.showPdfPage(bitmap);
    }
}
