package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

import android.support.annotation.NonNull;

public interface IMessageDistributionService {
    Iterable<BaseMessage> getMessages();

    void put(@NonNull String message);

    void register(@NonNull OnMessageParsedCallback listener);

    void unregister(@NonNull OnMessageParsedCallback listener);
}
