package de.lmu.msp.gettogether.Presentation;

public interface IDocumentViewer {
    void onDocumentLoaded(IDocument document);
    void onDocumentLoadFailed();
}
