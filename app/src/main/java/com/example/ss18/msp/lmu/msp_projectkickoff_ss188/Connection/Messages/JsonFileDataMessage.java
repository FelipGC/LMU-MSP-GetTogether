package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Messages;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageType;

public class JsonFileDataMessage extends BaseMessage {
    private final long fileId;
    private final String fileName;

    public long getFileId() {
        return fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public JsonFileDataMessage(long fileId, String fileName) {
        super(MessageType.FILE);
        this.fileId = fileId;
        this.fileName = fileName;
    }
}
