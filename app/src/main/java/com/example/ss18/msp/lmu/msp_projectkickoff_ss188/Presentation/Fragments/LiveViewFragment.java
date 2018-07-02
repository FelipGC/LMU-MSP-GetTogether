package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Fragments;

import android.arch.lifecycle.Observer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.IService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver.IOnMessageListener;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.MessageReceiver.OnMessageListener;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.NearbyDiscoveryService;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase.AppPreferences;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.OnMessageParsedCallback;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.IDocument;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationFileRequestMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationNoneMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStateMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStateRequestMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStopMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.R;

import java.io.File;

public class LiveViewFragment extends AbstractPresentationFragment {
    private static final String TAG = "LiveViewFragment";

    private File file = null;
    private String transferId = null;
    private IOnMessageListener fileListener = new FileListener();
    private OnMessageParsedCallback messageHandler = new MessageHandler();

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_live_view;
    }

    @Override
    protected Class<? extends IService> getServiceClass() {
        return NearbyDiscoveryService.class;
    }

    @Override
    protected void initButtons(View view) {
        startPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerForPresentation();
            }
        });

        stopPresentationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPresentation();
            }
        });
    }

    private void registerForPresentation() {
        if (connectionService == null) {
            return;
        }
        String sender = AppPreferences.getInstance(context).getUsername();
        JsonPresentationFileRequestMessage message = new JsonPresentationFileRequestMessage(sender);
        transferId = message.getTransferId();
        connectionService.broadcastMessage(message.toJsonString());
    }

    public void stopPresentation() {
        model.stopPresentation();
    }

    @Override
    protected void addStateListeners() {
        super.addStateListeners();
        model.getDocument().observe(this, new DocumentObserver());
    }

    @Override
    protected void onConnectionServiceConnected() {
        super.onConnectionServiceConnected();
        connectionService.register(fileListener);
    }

    @Override
    protected void onMessageDistributionServiceConnected() {
        super.onMessageDistributionServiceConnected();
        messageDistributionService.register(messageHandler);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (connectionService != null) {
            connectionService.unregister(fileListener);
        }
        if (messageDistributionService != null) {
            messageDistributionService.unregister(messageHandler);
        }
    }

    private class FileListener extends OnMessageListener {
        @Override
        public void onFileReceived(File file, String filename) {
            if (transferId == null) {
                return;
            }
            if (!filename.contentEquals(transferId)) {
                return;
            }
            transferId = null;
            LiveViewFragment.this.file = file;
            Uri uri = Uri.fromFile(file);
            model.loadDocument(uri, context.getContentResolver());
        }
    }

    private class DocumentObserver implements Observer<IDocument> {
        @Override
        public void onChanged(@Nullable IDocument document) {
            if (document != null) {
                sendStateRequest();
            } else {
                clearDocument();
            }
        }

        private void sendStateRequest() {
            if (connectionService == null) {
                return;
            }
            String sender = AppPreferences.getInstance(context).getUsername();
            BaseMessage message = new JsonPresentationStateRequestMessage(sender);
            connectionService.broadcastMessage(message.toJsonString());
        }

        private void clearDocument() {
            if (!file.delete()) {
                Log.i(TAG, "Could not delete file: "+ file.getName());
            }
            file = null;
        }
    }

    private class MessageHandler implements OnMessageParsedCallback {
        @Override
        public void onMessageParsed(@NonNull BaseMessage message) {
            if (message instanceof JsonPresentationStateMessage) {
                JsonPresentationStateMessage stateMessage = (JsonPresentationStateMessage) message;
                int pageNr = stateMessage.getPageNr();
                model.goToPage(pageNr);
            } else if (message instanceof JsonPresentationStopMessage) {
                stopPresentation();
            } else if (message instanceof JsonPresentationNoneMessage) {
                String noPresentationMessage = getString(
                        R.string.presentation_noPresentationAvailable);
                context.displayShortMessage(noPresentationMessage);
            }
        }
    }
}
