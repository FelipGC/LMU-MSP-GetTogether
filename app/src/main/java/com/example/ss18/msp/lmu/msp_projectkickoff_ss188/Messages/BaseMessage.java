package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

import com.google.gson.Gson;

public class BaseMessage {

    protected String id;
    private String sender;
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

    BaseMessage(String id, String sender, MessageType type){
        this.id = id;
        this.sender = sender;
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
