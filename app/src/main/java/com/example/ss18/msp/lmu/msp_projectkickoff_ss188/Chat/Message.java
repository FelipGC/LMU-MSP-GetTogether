package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat;

import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users.User;

public class Message {

    private String text; // message body
    private String userName; // data of the user that sent this message
    private boolean belongsToCurrentUser; // is this message sent by us?

    public Message(String text, String userName, boolean belongsToCurrentUser) {
        this.text = text;
        this.userName = userName;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public String getData() {
        return userName;
    }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }
}
