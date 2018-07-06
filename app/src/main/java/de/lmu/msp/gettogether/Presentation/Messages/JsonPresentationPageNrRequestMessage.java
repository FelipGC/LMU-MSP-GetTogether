package de.lmu.msp.gettogether.Presentation.Messages;

import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Messages.MessageType;

public class JsonPresentationPageNrRequestMessage extends BaseMessage {

    public JsonPresentationPageNrRequestMessage() {
        super(MessageType.PRESENTATION_PAGE_NR_REQUEST);
    }
}
