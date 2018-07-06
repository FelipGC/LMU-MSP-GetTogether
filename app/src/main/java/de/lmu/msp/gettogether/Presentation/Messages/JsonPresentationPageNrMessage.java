package de.lmu.msp.gettogether.Presentation.Messages;

import de.lmu.msp.gettogether.Messages.BaseMessage;
import de.lmu.msp.gettogether.Messages.MessageType;

public class JsonPresentationPageNrMessage extends BaseMessage {
    private int pageNr;

    public int getPageNr() {
        return pageNr;
    }

    public JsonPresentationPageNrMessage(int pageNr) {
        super(MessageType.PRESENTATION_STATE);
        this.pageNr = pageNr;
    }
}
