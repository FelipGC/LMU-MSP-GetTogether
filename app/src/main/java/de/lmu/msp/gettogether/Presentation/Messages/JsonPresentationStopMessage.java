package de.lmu.msp.gettogether.Presentation.Messages;

import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Messages.MessageType;

public class JsonPresentationStopMessage extends BaseMessage {

    public JsonPresentationStopMessage() {
        super(MessageType.PRESENTATION_STOP);
    }
}
