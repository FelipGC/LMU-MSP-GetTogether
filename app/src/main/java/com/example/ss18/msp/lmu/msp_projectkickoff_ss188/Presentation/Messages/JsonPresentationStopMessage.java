package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageType;

public class JsonPresentationStopMessage extends BaseMessage {

    public JsonPresentationStopMessage() {
        super(MessageType.PRESENTATION_STOP);
    }
}
