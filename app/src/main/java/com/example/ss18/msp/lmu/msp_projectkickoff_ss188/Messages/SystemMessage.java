package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages;

public class SystemMessage extends BaseMessage {

    private String content;

    public SystemMessage(String content){
        super(MessageType.SYSTEM);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
