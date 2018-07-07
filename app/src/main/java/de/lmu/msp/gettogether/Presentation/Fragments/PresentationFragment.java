package de.lmu.msp.gettogether.Presentation.Fragments;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.io.FileNotFoundException;

import de.lmu.msp.gettogether.Connection.IMessageListener;
import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Presentation.IDocument;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationFileNameMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationNoActiveMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationPageNrMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationFileNameRequestMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationPageNrRequestMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationStopMessage;
import de.lmu.msp.gettogether.Presentation.ShowViewObserver;
import de.lmu.msp.gettogether.R;

public class PresentationFragment extends AbstractPresentationFragment {
    private String fileName = null;
    private View nextPageButton;
    private View previousPageButton;
    private IMessageListener messageListener = new MessageListener();

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_presentation;
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
        if (documentUri == null) {
            context.displayShortMessage(getString(R.string.presentation_couldNotOpenFile));
            return;
        }
        model.loadDocument(documentUri, context.getContentResolver());
    }

    @Override
    protected void onConnectionManagerConnected() {
        super.onConnectionManagerConnected();
        if (connectionManager == null) {
            return;
        }
        connectionManager.getPayloadReceiver().register(messageListener);
    }

    @Override
    protected void addStateListeners() {
        super.addStateListeners();
        model.getShowNextButton().observe(this, new ShowViewObserver(nextPageButton));
        model.getShowPreviousButton().observe(this, new ShowViewObserver(previousPageButton));
        model.getDocument().observe(this, new DocumentObserver());
        model.getActivePageNr().observe(this, new ActivePageNrObserver());
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
    public void onDestroy() {
        if (connectionManager != null) {
            connectionManager.getPayloadReceiver().unregister(messageListener);
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        model.getShowNextButton().removeObservers(this);
        model.getShowPreviousButton().removeObservers(this);
        model.getDocument().removeObservers(this);
        model.getActivePageNr().removeObservers(this);
    }

    private void sendPageNrMessage(@NonNull Integer pageNr) {
        if (connectionManager == null) {
            return;
        }
        BaseMessage stateMessage = new JsonPresentationPageNrMessage(pageNr);
        connectionManager.getPayloadSender().sendMessage(stateMessage.toJsonString());
    }

    private class ActivePageNrObserver implements Observer<Integer> {
        @Override
        public void onChanged(@Nullable Integer pageNr) {
            if (pageNr == null) {
                return;
            }
            sendPageNrMessage(pageNr);
        }
    }

    private class DocumentObserver implements Observer<IDocument> {
        @Override
        public void onChanged(@Nullable IDocument document) {
            if (document != null) {
                fileName = document.getFileName();
                sendFile(document);
                return;
            }
            if (connectionManager == null) {
                return;
            }
            BaseMessage stopMessage = new JsonPresentationStopMessage();
            connectionManager.getPayloadSender().sendMessage(stopMessage.toJsonString());
        }

        private void sendFile(@NonNull IDocument document) {
            if (connectionManager == null) {
                return;
            }
            String fileName = document.getFileName();
            Uri uri = document.getUri();
            try {
                ParcelFileDescriptor fileDescriptor =
                        context.getContentResolver().openFileDescriptor(uri, "r");
                connectionManager.getPayloadSender().sendFile(fileName, fileDescriptor);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class MessageListener implements IMessageListener {

        @Override
        public void onMessageReceived(String message) {
            BaseMessage baseMessage = BaseMessage.fromJsonString(message);
            if (baseMessage == null) {
                return;
            }
            if (baseMessage instanceof JsonPresentationFileNameRequestMessage) {
                sendFileNameMessage();
            } else if (baseMessage instanceof JsonPresentationPageNrRequestMessage) {
                responseToPageNrRequest();
            }
        }

        private void responseToPageNrRequest() {
            Integer pageNr = model.getActivePageNr().getValue();
            if (pageNr == null) {
                sendNoActivePresentationMessage();
                return;
            }
            sendPageNrMessage(pageNr);
        }

        private void sendFileNameMessage() {
            if (connectionManager == null) {
                return;
            }
            if (fileName == null) {
                sendNoActivePresentationMessage();
                return;
            }
            BaseMessage fileNameMessage = new JsonPresentationFileNameMessage(fileName);
            connectionManager.getPayloadSender().sendMessage(fileNameMessage.toJsonString());
        }

        private void sendNoActivePresentationMessage() {
            if (connectionManager == null) {
                return;
            }
            BaseMessage noActiveMessage = new JsonPresentationNoActiveMessage();
            connectionManager.getPayloadSender().sendMessage(noActiveMessage.toJsonString());
        }
    }
}
