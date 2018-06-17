package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.AsyncTaskResult;

public class PresentationViewModel extends ViewModel implements IDocumentViewer {
    private MutableLiveData<Bitmap> activePage;
    private MutableLiveData<Integer> message;
    private IDocument document;

    public PresentationViewModel() {
        super();
        activePage = new MutableLiveData<>();
        message = new MutableLiveData<>();
    }

    public LiveData<Bitmap> getActivePage() {
        return activePage;
    }

    public LiveData<Integer> getMessage() {
        return message;
    }

    public void loadDocument(Uri documentUri, ContentResolver contentResolver) {
        AsyncTask<Uri, Void, AsyncTaskResult<IDocument>> loadDocumentTask = new LoadDocumentTask(this, contentResolver);
        loadDocumentTask.execute(documentUri);
    }

    @Override
    public void onDocumentLoaded(IDocument document) {
        this.document = document;
        goToPage(0);
    }

    @Override
    public void onDocumentLoadFailed() {
        message.setValue(R.string.presentation_openDocumentErrorMessage);
    }

    public void goToPage(int pageNr) {
        if (document == null) {
            return;
        }
        Bitmap page = document.getPage(pageNr);
        activePage.setValue(page);
    }

    public void stopPresentation() {
        this.document = null;
        activePage.setValue(null);
    }
}
