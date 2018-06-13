package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

public interface IDocumentViewer {
    void showDocument(IDocument document);
    void showErrorToast();
    void goToPage(int pageNr);
}
