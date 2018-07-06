package de.lmu.msp.gettogether.Presentation.Fragments;

import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import de.lmu.msp.gettogether.Connection.IMessageListener;
import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Presentation.IDocument;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationFileNameMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationNoActiveMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationPageNrMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationFileNameRequestMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationPageNrRequestMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationStopMessage;
import de.lmu.msp.gettogether.R;

public class LiveViewFragment extends AbstractPresentationFragment {
    private IMessageListener messageListener = new MessageListener();

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_live_view;
    }

    @Override
    protected void initButtons(View view) {
        startPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFileName();
            }
        });
        stopPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPresentation();
            }
        });
    }

    @Override
    protected void onConnectionManagerConnected() {
        super.onConnectionManagerConnected();
        if (connectionManager == null) {
            return;
        }
        connectionManager.getPayloadReceiver().register(messageListener);
    }

    private void requestFileName() {
        if (connectionManager == null) {
            return;
        }
        BaseMessage stateRequestMessage = new JsonPresentationFileNameRequestMessage();
        connectionManager.getPayloadSender().sendMessage(stateRequestMessage.toJsonString());
    }

    public void stopPresentation() {
        model.stopPresentation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (connectionManager != null) {
            connectionManager.getPayloadReceiver().unregister(messageListener);
        }
    }

    @Override
    protected void addStateListeners() {
        super.addStateListeners();
        model.getDocument().observe(this, new DocumentObserver());
    }

    private class DocumentObserver implements Observer<IDocument> {
        @Override
        public void onChanged(@Nullable IDocument document) {
            if (document != null) {
                requestActualPageNr();
            } else {
                fileName = null;
            }
        }

        private void requestActualPageNr() {
            if (connectionManager == null) {
                return;
            }
            BaseMessage pageNrRequestMessage = new JsonPresentationPageNrRequestMessage();
            connectionManager.getPayloadSender().sendMessage(pageNrRequestMessage.toJsonString());
        }
    }

    private class MessageListener implements IMessageListener {

        @Override
        public void onMessageReceived(String message) {
            BaseMessage baseMessage = BaseMessage.fromJsonString(message);
            if (baseMessage == null) {
                return;
            }
            if (baseMessage instanceof JsonPresentationFileNameMessage) {
                loadFile((JsonPresentationFileNameMessage) baseMessage);
            } else if (baseMessage instanceof JsonPresentationPageNrMessage) {
                changePage((JsonPresentationPageNrMessage) baseMessage);
            } else if (baseMessage instanceof JsonPresentationStopMessage) {
                stopPresentation();
            } else if (baseMessage instanceof JsonPresentationNoActiveMessage) {
                context.displayShortMessage(
                        getString(R.string.presentation_noPresentationAvailable));
            }
        }

        private void loadFile(@NonNull JsonPresentationFileNameMessage fileNameMessage) {
            String fileName = fileNameMessage.getFileName();
            Uri uri = fileNameToUri(fileName);
            model.loadDocument(uri, context.getContentResolver());
        }

        private void changePage(@NonNull JsonPresentationPageNrMessage pageNrMessage) {
            int pageNr = pageNrMessage.getPageNr();
            model.goToPage(pageNr);
        }

        private Uri fileNameToUri(String fileName) {
            // TODO: Get Uri from fileName
            return null;
        }
    }
}
