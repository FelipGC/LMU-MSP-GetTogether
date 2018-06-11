package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.app.Activity;
import android.content.ContentResolver;
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

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AppContext} interface
 * to handle interaction events.
 */
public class PresentationFragment extends Fragment implements IPdfViewer {
    private AppContext context;
    private View startPresentationButton;
    private View stopPresentationButton;
    private ImageView pdfView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_presentation, container, false);
        // TODO: Load previous state.
        initializeButtons(view);
        pdfView = view.findViewById(R.id.presentation_pdfView);
        return view;
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
        // TODO: Remove PDF View
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
        // TODO: Refactor, split task into pdfrenderer creation and bitmap creation for paging.
        AsyncTask<Uri, Void, Bitmap> loadPdfTask = new LoadPdfTask(this);
        loadPdfTask.execute(documentUri);
    }

    @Override
    public void showPdfPage(Bitmap bitmap) {
        pdfView.setImageBitmap(bitmap);
        pdfView.setVisibility(View.VISIBLE);
        stopPresentationButton.setVisibility(View.VISIBLE);
        startPresentationButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showErrorToast() {
        int errorMessageId = R.string.presentation_openDocumentErrorMessage;
        String errorMessage = context.getString(errorMessageId);
        context.displayShortMessage(errorMessage);
    }

    @Override
    public ContentResolver getContentResolver() {
        return context.getContentResolver();
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
        // TODO: Save state (url, pageNr)
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
