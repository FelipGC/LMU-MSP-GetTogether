package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Presentation.Messages;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.BaseMessage;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Messages.MessageType;

import java.security.SecureRandom;

public class JsonPresentationFileRequestMessage extends BaseMessage {
    private static final String CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    private final String transferId;
    private final String sender;

    public String getTransferId() {
        return transferId;
    }

    public String getSender() {
        return sender;
    }

    public JsonPresentationFileRequestMessage(String sender) {
        super(MessageType.PRESENTATION_FILE_REQUEST);
        transferId = randomString();
        this.sender = sender;
    }

    private String randomString() {
        int digits = 10 + rnd.nextInt(11);
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++)
            sb.append(CHARS.charAt(rnd.nextInt(CHARS.length())));
        return sb.toString();
    }
}
