package de.lmu.msp.gettogether.Presentation.Messages;

import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Messages.MessageType;

public class JsonPresentationFileNameRequestMessage extends BaseMessage {

    public JsonPresentationFileNameRequestMessage() {
        super(MessageType.PRESENTATION_FILE_NAME_REQUEST);
    }
}
