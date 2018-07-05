package de.lmu.msp.gettogether.Chat;

import android.support.annotation.Nullable;

public class Message {

    private String text; // message body
    private String userName; // data of the user that sent this message
    private boolean belongsToCurrentUser; // is this message sent by us?
    private final String id;

    public Message(String text, @Nullable String id, String userName, boolean belongsToCurrentUser) {
        this.text = text;
        this.userName = userName;
        this.belongsToCurrentUser = belongsToCurrentUser;
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public String getUserName() {
        return userName;
    }

    public String getId() {
        return id;
    }
    public boolean belongsToCurrentUser() {
        return belongsToCurrentUser;
    }
}
