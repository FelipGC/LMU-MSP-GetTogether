package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

import com.google.gson.Gson;

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

    public static BaseMessage fromJsonString(String json){
        Gson gson = new Gson();
        BaseMessage msg = gson.fromJson(json, BaseMessage.class);
        Class<? extends BaseMessage> c;
        switch (msg.getType()){
            case CHAT:
                c = ChatMessage.class;
                break;
            case FILE:
            case POKE:
            case LOCATION:
            default:
                c = BaseMessage.class;
                break;
        }
        return gson.fromJson(json, c);
    }
}
