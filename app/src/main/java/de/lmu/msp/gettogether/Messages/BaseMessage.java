package de.lmu.msp.gettogether.Messages;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationFileNameMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationNoActiveMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationPageNrMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationFileNameRequestMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationPageNrRequestMessage;
import de.lmu.msp.gettogether.Presentation.Messages.JsonPresentationStopMessage;

public class BaseMessage {
    protected MessageType type;

    public MessageType getType() {
        return type;
    }

    protected BaseMessage(MessageType type){
        this.type = type;
    }

    public String toJsonString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Nullable
    public static BaseMessage fromJsonString(String json){
        Gson gson = new Gson();
        try {
            BaseMessage msg = gson.fromJson(json, BaseMessage.class);
            Class<? extends BaseMessage> c;
            switch (msg.getType()) {
                case PRESENTATION_STATE:
                    c = JsonPresentationPageNrMessage.class;
                    break;
                case PRESENTATION_FILE_NAME_REQUEST:
                    c = JsonPresentationFileNameRequestMessage.class;
                    break;
                case PRESENTATION_STOP:
                    c = JsonPresentationStopMessage.class;
                    break;
                case PRESENTATION_FILE_NAME:
                    c = JsonPresentationFileNameMessage.class;
                    break;
                case PRESENTATION_PAGE_NR_REQUEST:
                    c = JsonPresentationPageNrRequestMessage.class;
                    break;
                case PRESENTATION_NO_ACTIVE:
                    c = JsonPresentationNoActiveMessage.class;
                    break;
                default:
                    c = BaseMessage.class;
                    break;
            }
            return gson.fromJson(json, c);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
