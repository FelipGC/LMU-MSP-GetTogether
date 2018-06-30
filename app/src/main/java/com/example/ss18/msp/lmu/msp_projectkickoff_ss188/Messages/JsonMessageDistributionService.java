package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class JsonMessageDistributionService extends Service implements IMessageDistributionService {
    private MessageDistributionBinder binder =
            new MessageDistributionBinder(this);
    private Collection<OnMessageParsedCallback> messageParsedListeners = new ArrayList<>();
    private Collection<BaseMessage> messages = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public Collection<BaseMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    @Override
    public void put(@NonNull String message) {
        BaseMessage baseMessage = BaseMessage.fromJsonString(message);
        if (baseMessage == null) {
            return;
        }
        messages.add(baseMessage);
        for (OnMessageParsedCallback listener :
                messageParsedListeners) {
            listener.onMessageParsed(baseMessage);
        }
    }

    @Override
    public void register(@NonNull OnMessageParsedCallback listener) {
        messageParsedListeners.add(listener);
    }

    @Override
    public void unregister(@NonNull OnMessageParsedCallback listener) {
        messageParsedListeners.remove(listener);
    }
}
