package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageType;

public class JsonPresentationStateRequestMessage extends BaseMessage {

    private final String sender;

    public String getSender() {
        return sender;
    }

    public JsonPresentationStateRequestMessage(String sender) {
        super(MessageType.PRESENTATION_STATE_REQUEST);
        this.sender = sender;
    }
}
