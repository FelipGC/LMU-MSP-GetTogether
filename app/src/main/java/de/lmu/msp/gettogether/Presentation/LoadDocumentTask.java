package de.lmu.msp.gettogether.Presentation;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;

import de.lmu.msp.gettogether.Utility.AsyncTaskResult;

public class LoadDocumentTask extends AsyncTask<Uri, Void, AsyncTaskResult<IDocument>> {
    private final ContentResolver contentResolver;
    private IDocumentLoadCallback documentLoadCallback;

    LoadDocumentTask(IDocumentLoadCallback documentLoadCallback, ContentResolver contentResolver) {
        this.documentLoadCallback = documentLoadCallback;
        this.contentResolver = contentResolver;
    }

    @Override
    protected AsyncTaskResult<IDocument> doInBackground(Uri... params) {
        Uri documentUri = params[0];
        try {
            IDocument document = RenderedPdfDocument.load(documentUri, contentResolver); // TODO: Extract RenderedPdfDocument dependency
            return new AsyncTaskResult<>(document);
        }
        catch (IOException ex) {
            return new AsyncTaskResult<>(ex);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<IDocument> result) {
        super.onPostExecute(result);
        if (result.getError() != null) {
            documentLoadCallback.onDocumentLoadFailed();
            return;
        }
        documentLoadCallback.onDocumentLoaded(result.getResult());
    }
}
