package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

import android.os.Binder;
import android.support.annotation.NonNull;

public class MessageDistributionBinder extends Binder {
    private IMessageDistributionService messageDistributionService;

    MessageDistributionBinder(@NonNull IMessageDistributionService messageDistributionService) {
        this.messageDistributionService = messageDistributionService;
    }

    public IMessageDistributionService getService() {
        return messageDistributionService;
    }
}
