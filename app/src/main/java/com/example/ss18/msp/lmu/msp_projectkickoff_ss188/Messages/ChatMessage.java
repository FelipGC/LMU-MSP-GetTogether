package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

public class ChatMessage extends BaseMessage {
    private String id;
    private String sender;
    private String body;

    public String getId() {
        return id;
    }

    public String getSender() {
        return sender;
    }

    public String getBody() {
        return body;
    }

    public ChatMessage(String id, String sender, String body) {
        super(MessageType.CHAT);
        this.id = id;
        this.sender = sender;
        this.body = body;
    }
}
