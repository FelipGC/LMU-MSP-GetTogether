package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Messages;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageType;

public class JsonFileDataMessage extends BaseMessage {
    private final long payloadId;
    private final String fileName;

    public long getPayloadId() {
        return payloadId;
    }

    public String getFileName() {
        return fileName;
    }

    public JsonFileDataMessage(long payloadId, String fileName) {
        super(MessageType.FILE);
        this.payloadId = payloadId;
        this.fileName = fileName;
    }
}
