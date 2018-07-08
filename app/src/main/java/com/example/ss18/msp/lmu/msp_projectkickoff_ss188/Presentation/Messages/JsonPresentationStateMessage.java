package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageType;

public class JsonPresentationStateMessage extends BaseMessage {

    private int pageNr;

    public JsonPresentationStateMessage(int pageNr) {
        super(MessageType.PRESENTATION_STATE);
        this.pageNr = pageNr;
    }

    public int getPageNr() {
        return pageNr;
    }
}
