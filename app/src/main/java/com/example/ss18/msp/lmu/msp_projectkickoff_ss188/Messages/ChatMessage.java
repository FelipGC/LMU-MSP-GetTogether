package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

public class ChatMessage extends BaseMessage {

    private String body;

    public String getBody() {
        return body;
    }

    public ChatMessage(String id, String sender, String body){
        super(id, sender, MessageType.CHAT);
        this.body = body;
    }
}
