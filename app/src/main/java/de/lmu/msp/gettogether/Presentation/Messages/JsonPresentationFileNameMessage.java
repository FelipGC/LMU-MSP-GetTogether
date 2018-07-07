package de.lmu.msp.gettogether.Presentation.Messages;

import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Messages.MessageType;

public class JsonPresentationFileNameMessage extends BaseMessage {
    private final String fileName;

    public String getFileName() {
        return fileName;
    }

    public JsonPresentationFileNameMessage(String fileName) {
        super(MessageType.PRESENTATION_FILE_NAME);
        this.fileName = fileName;
    }
}
