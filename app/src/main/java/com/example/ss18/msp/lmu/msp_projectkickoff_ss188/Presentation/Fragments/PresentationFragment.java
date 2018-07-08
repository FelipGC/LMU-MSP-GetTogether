package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.IService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.NearbyAdvertiseService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.OnMessageParsedCallback;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.IDocument;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationFileRequestMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationNoneMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStateMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStateRequestMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStopMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.ShowViewObserver;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Utility.AppContext;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AppContext} interface
 * to handle interaction events.
 */
public class PresentationFragment extends AbstractPresentationFragment {
    private View nextPageButton;
    private View previousPageButton;
    private OnMessageParsedCallback messageHandler = new MessageHandler();

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_presentation;
    }

    @Override
    protected Class<? extends IService> getServiceClass() {
        return NearbyAdvertiseService.class;
    }

    @Override
    protected void initButtons(View view) {
        previousPageButton = view.findViewById(R.id.presentation_previousPageButton);
        nextPageButton = view.findViewById(R.id.presentation_nextPageButton);
        startPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDocumentToPresent();
            }
        });
        stopPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPresentation();
            }
        });
        previousPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPreviousPage();
            }
        });
        nextPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextPage();
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

    private void goToPreviousPage() {
        model.gotToPreviousPage();
    }

    private void goToNextPage() {
        model.goToNextPage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != getOpenPresentationRequestCode()) {
            return;
        }
        if (resultCode != Activity.RESULT_OK || data == null) {
            showErrorToast();
            return;
        }
        Uri documentUri = data.getData();
        model.loadDocument(documentUri, context.getContentResolver());
    }

    @Override
    protected void addStateListeners() {
        super.addStateListeners();
        model.getShowNextButton().observe(this, new ShowViewObserver(nextPageButton));
        model.getShowPreviousButton().observe(this, new ShowViewObserver(previousPageButton));
        model.getDocument().observe(this, new DocumentObserver());
        model.getActivePageNr().observe(this, new ActivePageNrObserver());
    }

    @Override
    protected void onMessageDistributionServiceConnected() {
        super.onMessageDistributionServiceConnected();
        messageDistributionService.register(messageHandler);
    }

    private void showErrorToast() {
        int errorMessageId = R.string.presentation_openDocumentErrorMessage;
        String errorMessage = getString(errorMessageId);
        context.displayShortMessage(errorMessage);
    }

    private int getOpenPresentationRequestCode() {
        int resId = R.string.presentation_openPresentationRequestCode;
        String requestCode = getString(resId);
        return Integer.valueOf(requestCode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (messageDistributionService != null) {
            messageDistributionService.unregister(messageHandler);
        }
    }

    private class ActivePageNrObserver implements Observer<Integer> {
        @Override
        public void onChanged(@Nullable Integer pageNr) {
            if (pageNr == null) {
                return;
            }
            broadcastStateMessage(pageNr);
        }

        private void broadcastStateMessage(@NonNull Integer pageNr) {
            if (connectionService == null) {
                return;
            }
            JsonPresentationStateMessage stateMessage = new JsonPresentationStateMessage(pageNr);
            connectionService.broadcastMessage(stateMessage.toJsonString());
        }
    }

    private class DocumentObserver implements Observer<IDocument> {
        @Override
        public void onChanged(@Nullable IDocument document) {
            if (document != null) {
                return;
            }
            broadcastStopMessage();
        }

        private void broadcastStopMessage() {
            if (connectionService == null) {
                return;
            }
            JsonPresentationStopMessage stopMessage = new JsonPresentationStopMessage();
            connectionService.broadcastMessage(stopMessage.toJsonString());
        }
    }

    private class MessageHandler implements OnMessageParsedCallback {
        @Override
        public void onMessageParsed(@NonNull BaseMessage message) {
            if (connectionService == null) {
                return;
            }
            if (message instanceof JsonPresentationFileRequestMessage) {
                sendFileResponse((JsonPresentationFileRequestMessage) message);
            } else if (message instanceof JsonPresentationStateRequestMessage) {
                sendStateResponse((JsonPresentationStateRequestMessage) message);
            }
        }

        private void sendStateResponse(@NonNull JsonPresentationStateRequestMessage message) {
            String sender = message.getSender();
            Integer pageNr = model.getActivePageNr().getValue();
            if (pageNr == null) {
                sendNoPresentationMessage(sender);
                return;
            }
            BaseMessage stateMessage = new JsonPresentationStateMessage(pageNr);
            connectionService.sendMessage(sender, stateMessage.toJsonString());
        }

        private void sendFileResponse(@NonNull JsonPresentationFileRequestMessage message) {
            String sender = message.getSender();
            IDocument document = model.getDocument().getValue();
            if (document == null) {
                sendNoPresentationMessage(sender);
                return;
            }
            String filePath = document.getUri().getPath();
            File file = new File(filePath);
            connectionService.sendFile(sender, file, message.getTransferId());
        }

        private void sendNoPresentationMessage(String sender) {
            BaseMessage noPresentationMessage = new JsonPresentationNoneMessage();
            connectionService.sendMessage(sender, noPresentationMessage.toJsonString());
        }
    }
}
