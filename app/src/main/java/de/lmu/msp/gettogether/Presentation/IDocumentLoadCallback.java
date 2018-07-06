package de.lmu.msp.gettogether.Presentation;

public interface IDocumentLoadCallback {
    void onDocumentLoaded(IDocument document);
    void onDocumentLoadFailed();
}
