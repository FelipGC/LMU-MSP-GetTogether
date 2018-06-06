package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.AppContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StartPresentationContext} interface
 * to handle interaction events.
 */
public class StartPresentationFragment extends Fragment {
    private StartPresentationContext context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_start_presentation, container, false);
        initializeButton(view);
        return view;
    }

    private void initializeButton(View view) {
        View openPresentationButton = view.findViewById(R.id.openPresentationButton);
        openPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDocumentToPresent();
            }
        });
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
        context.presentDocument(documentUri);
    }

    private void showErrorToast() {
        int errorMessageId = R.string.startPresentationFragment_openDocumentErrorMessage;
        String errorMessage = context.getStringById(errorMessageId);
        context.displayShortMessage(errorMessage);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof StartPresentationContext) {
            this.context = (StartPresentationContext) context;
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

    public int getOpenPresentationRequestCode() {
        int resId = R.string.startPresentationFragment_openPresentationRequestCode;
        String requestCode= context.getStringById(resId);
        return Integer.valueOf(requestCode);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface StartPresentationContext extends AppContext {
        void presentDocument(Uri uriToDocument);
    }
}
