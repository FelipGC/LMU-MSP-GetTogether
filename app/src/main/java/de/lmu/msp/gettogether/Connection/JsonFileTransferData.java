package de.lmu.msp.gettogether.Connection;

import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Messages.MessageType;

public class JsonFileTransferData extends BaseMessage {
    private final String fileName;
    private final long payloadId;

    public long getPayloadId() {
        return payloadId;
    }

    public String getFileName() {
        return fileName;
    }

    JsonFileTransferData(String fileName, long payloadId) {
        super(MessageType.CONNECTION_FILE_TRANSFER_DATA);
        this.fileName = fileName;
        this.payloadId = payloadId;
    }
}
