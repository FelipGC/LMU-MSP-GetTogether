package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;

public class LoadDocumentTask extends AsyncTask<Uri, Void, IDocument> {
    private final ContentResolver contentResolver;
    private IDocumentViewer documentViewer;

    LoadDocumentTask(IDocumentViewer documentViewer, ContentResolver contentResolver) {
        this.documentViewer = documentViewer;
        this.contentResolver = contentResolver;
    }

    @Override
    protected IDocument doInBackground(Uri... params) {
        Uri documentUri = params[0];
        return RenderedPdfDocument.load(documentUri, contentResolver); // TODO: Extract RenderedPdfDocument dependency
    }

    @Override
    protected void onPostExecute(IDocument document) {
        super.onPostExecute(document);
        if (document == null) {
            documentViewer.showErrorToast();
            return;
        }
        documentViewer.showDocument(document);
    }
}
