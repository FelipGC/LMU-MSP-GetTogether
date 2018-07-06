package de.lmu.msp.gettogether.Presentation.Messages;

import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Messages.MessageType;

public class JsonPresentationNoActiveMessage extends BaseMessage {

    public JsonPresentationNoActiveMessage() {
        super(MessageType.PRESENTATION_NO_ACTIVE);
    }
}
