package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

import com.google.gson.Gson;

public class BaseMessage {

    protected String id;
    protected String sender;
    protected MessageType type;


    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    protected BaseMessage(String id, String sender, MessageType type){
        this.id = id;
        this.sender = sender;
        this.type = type;
    }

    public String toJsonString(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    public static <T extends BaseMessage> T fromJsonString(String json){
        Gson gson = new Gson();
        BaseMessage msg = gson.fromJson(json, BaseMessage.class);
        Class type = BaseMessage.class;
        switch (msg.getType()){
            case CHAT:
                type = ChatMessage.class;
                break;
            case FILE:
            case POKE:
            case LOCATION:
                break;
        }

        return (T) type.cast(gson.fromJson(json,type));
    }
}
