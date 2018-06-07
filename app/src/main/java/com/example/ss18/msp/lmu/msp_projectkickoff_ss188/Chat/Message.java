package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Chat;

import android.graphics.Bitmap;

public class Message {

    private String text; // message body
    private String userName; // data of the user that sent this message
    private Bitmap profilePicture; //image of the user that sent this message
    private boolean belongsToCurrentUser; // is this message sent by us?

    public Message(String text, String userName, Bitmap profilePicture, boolean belongsToCurrentUser) {
        this.text = text;
        this.userName = userName;
        this.profilePicture = profilePicture;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public String getUserName() {
        return userName;
    }

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public boolean belongsToCurrentUser() {
        return belongsToCurrentUser;
    }
}
