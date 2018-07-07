package de.lmu.msp.gettogether.Presentation.Fragments;

import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.io.File;

import de.lmu.msp.gettogether.Connection.IMessageListener;
import de.lmu.msp.gettogether.DataBase.FileService;
import de.lmu.msp.gettogether.DataBase.IFileService;
import de.lmu.msp.gettogether.DataBase.IFileServiceBinder;
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
    private ServiceConnection fileServiceConnection = new FileServiceConnection();
    private IFileService fileService = null;

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
    protected void bindServices() {
        super.bindServices();
        bindService(FileService.class, fileServiceConnection);
    }

    @Override
    protected void unbindServices() {
        super.unbindServices();
        unbindService(fileServiceConnection);
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
    public void onDestroy() {
        if (connectionManager != null) {
            connectionManager.getPayloadReceiver().unregister(messageListener);
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        model.getDocument().removeObservers(this);
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
            if (uri == null) {
                return;
            }
            model.loadDocument(uri, context.getContentResolver());
        }

        private void changePage(@NonNull JsonPresentationPageNrMessage pageNrMessage) {
            int pageNr = pageNrMessage.getPageNr();
            model.goToPage(pageNr);
        }

        @Nullable
        private Uri fileNameToUri(String fileName) {
            File file = fileService.getFileFor(fileName);
            if (file == null) {
                context.displayShortMessage(getString(R.string.presentation_presentationNotReceived));
                return null;
            }
            Uri uri = Uri.fromFile(file);
            if (uri == null) {
                context.displayShortMessage(getString(R.string.presentation_presentationNotReceived));
                return null;
            }
            return uri;
        }
    }

    private class FileServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IFileServiceBinder binder = (IFileServiceBinder) service;
            fileService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            fileService = null;
        }
    }
}
