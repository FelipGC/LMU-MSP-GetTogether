package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.AppContext;

// TODO: Extract ViewModel for surviving configuration changes.

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AppContext} interface
 * to handle interaction events.
 */
public class PresentationFragment extends Fragment implements IDocumentViewer {
    private AppContext context;
    private View startPresentationButton;
    private View stopPresentationButton;
    private ImageView pdfView;
    private IDocument document;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_presentation, container, false);
        initializeButtons(view);
        pdfView = view.findViewById(R.id.presentation_pdfView);
        reloadPage(savedInstanceState);
        return view;
    }

    private void reloadPage(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        String documentUriMessageKey = getString(R.string.presentation_argument_documentUri);
        String documentUriString = bundle.getString(documentUriMessageKey);
        if (documentUriString == null) {
            return;
        }
        Uri documentUri = Uri.parse(documentUriString);
        if (documentUri == null) {
            return;
        }
        String pageNrMessageKey = getString(R.string.presentation_argument_pageNr);
        int pageNr = bundle.getInt(pageNrMessageKey);
        showDocument(documentUri);
        goToPage(pageNr);
    }

    @Override
    public void goToPage(int pageNr) {
        Bitmap page = document.getPage(pageNr);
        pdfView.setImageBitmap(page);
    }

    private void initializeButtons(View view) {
        startPresentationButton = view.findViewById(R.id.presentation_startPresentationButton);
        startPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDocumentToPresent();
            }
        });
        stopPresentationButton = view.findViewById(R.id.presentation_stopPresentationButton);
        stopPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPresentation();
            }
        });
    }

    private void stopPresentation() {
        pdfView.setVisibility(View.INVISIBLE);
        document = null;
        stopPresentationButton.setVisibility(View.INVISIBLE);
        startPresentationButton.setVisibility(View.VISIBLE);
    }

    private void chooseDocumentToPresent() {
        Intent fileChooser = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        fileChooser.addCategory(Intent.CATEGORY_OPENABLE);
        fileChooser.setType("application/pdf");
        startActivityForResult(fileChooser, getOpenPresentationRequestCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != getOpenPresentationRequestCode()) {
            return;
        }
        if (resultCode != Activity.RESULT_OK || data == null){
            showErrorToast();
            return;
        }
        Uri documentUri = data.getData();
        showDocument(documentUri);
    }

    private void showDocument(Uri documentUri) {
        AsyncTask<Uri, Void, IDocument> loadDocumentTask = new LoadDocumentTask(this,
                context.getContentResolver());
        loadDocumentTask.execute(documentUri);
    }

    @Override
    public void showDocument(IDocument document) {
        this.document = document;
        Bitmap firstPage = document.getPage(0);
        pdfView.setImageBitmap(firstPage);
        pdfView.setVisibility(View.VISIBLE);
        stopPresentationButton.setVisibility(View.VISIBLE);
        startPresentationButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showErrorToast() {
        int errorMessageId = R.string.presentation_openDocumentErrorMessage;
        String errorMessage = getString(errorMessageId);
        context.displayShortMessage(errorMessage);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AppContext) {
            this.context = (AppContext) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement StartPresentationContext");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (document == null) {
            return;
        }
        outState.putString(getString(R.string.presentation_argument_documentUri),
                document.getUri().toString());
        outState.putInt(getString(R.string.presentation_argument_pageNr),
                document.getActualPageNr());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    private int getOpenPresentationRequestCode() {
        int resId = R.string.presentation_openPresentationRequestCode;
        String requestCode= context.getString(resId);
        return Integer.valueOf(requestCode);
    }
}
