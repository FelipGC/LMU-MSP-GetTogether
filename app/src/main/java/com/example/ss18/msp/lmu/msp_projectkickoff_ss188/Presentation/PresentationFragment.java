package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
public class PresentationFragment extends Fragment {
    private AppContext context;
    private View startPresentationButton;
    private View stopPresentationButton;
    private ImageView pdfView;
    private PresentationViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_presentation, container, false);
        initializeButtons(view);
        pdfView = view.findViewById(R.id.presentation_pdfView);
        initModel();
//        reloadPage(savedInstanceState);
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

    private void chooseDocumentToPresent() {
        Intent fileChooser = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        fileChooser.addCategory(Intent.CATEGORY_OPENABLE);
        fileChooser.setType("application/pdf");
        startActivityForResult(fileChooser, getOpenPresentationRequestCode());
    }

    private void stopPresentation() {
        model.stopPresentation();
    }

    private void goToPage(int pageNr) {
        model.goToPage(pageNr);
    }

    private void initModel() {
        model = ViewModelProviders.of(this)
                .get(PresentationViewModel.class);
        model.getActivePage().observe(this, new Observer<Bitmap>() {
            @Override
            public void onChanged(@Nullable Bitmap bitmap) {
                if (bitmap == null) {
                    resetView();
                }
                showPage(bitmap);
            }
        });
        model.getMessage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer id) {
                if (id == null) {
                    return;
                }
                context.displayShortMessage(getString(id));
            }
        });
//        Bitmap page = model.getActivePage().getValue();
//        if (page != null) {
//            showPage(page);
//        }
    }

    private void showPage(Bitmap bitmap) {
        pdfView.setVisibility(View.VISIBLE);
        pdfView.setImageBitmap(bitmap);
        stopPresentationButton.setVisibility(View.VISIBLE);
        startPresentationButton.setVisibility(View.INVISIBLE);
    }

    private void resetView() {
        pdfView.setImageResource(android.R.color.transparent);
        pdfView.setVisibility(View.INVISIBLE);
        stopPresentationButton.setVisibility(View.INVISIBLE);
        startPresentationButton.setVisibility(View.VISIBLE);
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
        model.loadDocument(documentUri, context.getContentResolver());
    }

    private void showErrorToast() {
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
    public void onDetach() {
        super.onDetach();
        context = null;
    }

    private int getOpenPresentationRequestCode() {
        int resId = R.string.presentation_openPresentationRequestCode;
        String requestCode= getString(resId);
        return Integer.valueOf(requestCode);
    }
}
