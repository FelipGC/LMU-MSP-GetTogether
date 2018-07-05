package de.lmu.msp.gettogether.Presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import de.lmu.msp.gettogether.R;
import de.lmu.msp.gettogether.Utility.AsyncTaskResult;

import de.lmu.msp.gettogether.Utility.AsyncTaskResult;

public class PresentationViewModel extends ViewModel implements IDocumentViewer {
    private MutableLiveData<Bitmap> activePage;
    private MutableLiveData<Integer> message;
    private MutableLiveData<Boolean> showNextButton;
    private MutableLiveData<Boolean> showPreviousButton;
    private MutableLiveData<Boolean> showStopButton;
    private MutableLiveData<Boolean> showStartButton;
    private IDocument document;

    public PresentationViewModel() {
        super();
        activePage = new MutableLiveData<>();
        message = new MutableLiveData<>();
        showNextButton = new MutableLiveData<>();
        showPreviousButton = new MutableLiveData<>();
        showStartButton = new MutableLiveData<>();
        showStopButton = new MutableLiveData<>();
    }

    public LiveData<Bitmap> getActivePage() {
        return activePage;
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

    public void loadDocument(Uri documentUri, ContentResolver contentResolver) {
        AsyncTask<Uri, Void, AsyncTaskResult<IDocument>> loadDocumentTask = new LoadDocumentTask(this, contentResolver);
        loadDocumentTask.execute(documentUri);
    }

    @Override
    public void onDocumentLoaded(IDocument document) {
        this.document = document;
        goToPage(0);
        showStartButton.setValue(false);
        showStopButton.setValue(true);
    }

    @Override
    public void onDocumentLoadFailed() {
        message.setValue(R.string.presentation_openDocumentErrorMessage);
    }

    private void goToPage(int pageNr) {
        if (document == null) {
            return;
        }
        if (pageNr >= document.getPageCount()) {
            return;
        }
        if (pageNr < 0) {
            return;
        }
        Bitmap page = document.getPage(pageNr);
        activePage.setValue(page);
        boolean showNextButton = pageNr != document.getPageCount() - 1;
        this.showNextButton.setValue(showNextButton);
        boolean showPrevButton = pageNr != 0;
        this.showPreviousButton.setValue(showPrevButton);
    }

    public void stopPresentation() {
        document = null;
        activePage.setValue(null);
        showNextButton.setValue(null);
        showPreviousButton.setValue(null);
        showStopButton.setValue(false);
        showStartButton.setValue(true);
    }

    public void goToNextPage() {
        int nextPageNr = document.getActualPageNr() + 1;
        goToPage(nextPageNr);
    }

    public void gotToPreviousPage() {
        int nextPageNr = document.getActualPageNr() - 1;
        goToPage(nextPageNr);
    }
}
