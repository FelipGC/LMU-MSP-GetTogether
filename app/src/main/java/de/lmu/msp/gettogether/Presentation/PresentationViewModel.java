package de.lmu.msp.gettogether.Presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import de.lmu.msp.gettogether.R;
import de.lmu.msp.gettogether.Utility.AsyncTaskResult;

public class PresentationViewModel extends ViewModel {
    private MutableLiveData<Bitmap> activePage;
    private MutableLiveData<Integer> activePageNr;
    private MutableLiveData<Integer> message;
    private MutableLiveData<Boolean> showNextButton;
    private MutableLiveData<Boolean> showPreviousButton;
    private MutableLiveData<Boolean> showStopButton;
    private MutableLiveData<Boolean> showStartButton;
    private MutableLiveData<IDocument> document;
    private IDocumentLoadCallback documentLoadCallback = new DocumentLoadCallback();

    public PresentationViewModel() {
        super();
        activePage = new MutableLiveData<>();
        activePageNr = new MutableLiveData<>();
        message = new MutableLiveData<>();
        showNextButton = new MutableLiveData<>();
        showPreviousButton = new MutableLiveData<>();
        showStartButton = new MutableLiveData<>();
        showStopButton = new MutableLiveData<>();
        document = new MutableLiveData<>();
    }

    public LiveData<Bitmap> getActivePage() {
        return activePage;
    }

    public LiveData<Integer> getActivePageNr() {
        return activePageNr;
    }

    public LiveData<Integer> getMessage() {
        return message;
    }

    public LiveData<Boolean> getShowNextButton() {
        return showNextButton;
    }

    public LiveData<Boolean> getShowPreviousButton() {
        return showPreviousButton;
    }

    public LiveData<Boolean> getShowStopButton() {
        return showStopButton;
    }

    public LiveData<Boolean> getShowStartButton() {
        return showStartButton;
    }

    public LiveData<IDocument> getDocument() {
        return document;
    }

    private IDocument getInternalDocument() {
        return document.getValue();
    }

    public void loadDocument(@NonNull Uri documentUri, ContentResolver contentResolver) {
        AsyncTask<Uri, Void, AsyncTaskResult<IDocument>> loadDocumentTask =
                new LoadDocumentTask(documentLoadCallback, contentResolver);
        loadDocumentTask.execute(documentUri);
    }

    public void goToPage(int pageNr) {
        if (getInternalDocument() == null) {
            return;
        }
        if (pageNr >= getInternalDocument().getPageCount()) {
            return;
        }
        if (pageNr < 0) {
            return;
        }
        Bitmap page = getInternalDocument().getPage(pageNr);
        boolean showNextButton = pageNr != getInternalDocument().getPageCount() - 1;
        boolean showPrevButton = pageNr != 0;
        activePage.setValue(page);
        activePageNr.setValue(pageNr);
        this.showNextButton.setValue(showNextButton);
        this.showPreviousButton.setValue(showPrevButton);
    }

    public void stopPresentation() {
        document.setValue(null);
        activePage.setValue(null);
        activePageNr.setValue(null);
        showNextButton.setValue(false);
        showPreviousButton.setValue(false);
        showStopButton.setValue(false);
        showStartButton.setValue(true);
    }

    public void goToNextPage() {
        if (document == null) {
            return;
        }
        Integer activePageNr = getActivePageNr().getValue();
        if (activePageNr == null) {
            return;
        }
        int nextPageNr = activePageNr + 1;
        goToPage(nextPageNr);
    }

    public void gotToPreviousPage() {
        if (document == null) {
            return;
        }
        Integer activePageNr = getActivePageNr().getValue();
        if (activePageNr == null) {
            return;
        }
        int nextPageNr = activePageNr - 1;
        goToPage(nextPageNr);
    }

    private class DocumentLoadCallback implements IDocumentLoadCallback {
        @Override
        public void onDocumentLoaded(IDocument document) {
            PresentationViewModel.this.document.setValue(document);
            goToPage(0);
            showStartButton.setValue(false);
            showStopButton.setValue(true);
        }

        @Override
        public void onDocumentLoadFailed() {
            message.setValue(R.string.presentation_openDocumentErrorMessage);
        }
    }
}
