package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

import android.support.annotation.Nullable;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.Messages.JsonFileDataMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationFileRequestMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationNoneMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStateMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStateRequestMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages.JsonPresentationStopMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
                case CHAT:
                    c = ChatMessage.class;
                    break;
                case FILE:
                    c = JsonFileDataMessage.class;
                    break;
                case PRESENTATION_FILE_REQUEST:
                    c = JsonPresentationFileRequestMessage.class;
                    break;
                case PRESENTATION_NONE:
                    c = JsonPresentationNoneMessage.class;
                    break;
                case PRESENTATION_STATE:
                    c = JsonPresentationStateMessage.class;
                    break;
                case PRESENTATION_STATE_REQUEST:
                    c = JsonPresentationStateRequestMessage.class;
                    break;
                case PRESENTATION_STOP:
                    c = JsonPresentationStopMessage.class;
                    break;
                case SYSTEM:
                    c = SystemMessage.class;
                    break;
                case POKE:
                case LOCATION:
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
