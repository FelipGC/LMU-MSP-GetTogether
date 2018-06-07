package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Inbox;
import com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection.ConnectionEndpoint;

import java.io.File;
import java.util.ArrayList;

public class InboxAdapterItem {
    private final ArrayList<File> files;
    private final ConnectionEndpoint connectionEndpoint;

    public InboxAdapterItem(ConnectionEndpoint endpoint, File file) {
        this.connectionEndpoint = endpoint;
        files = new ArrayList<>();
        files.add(file);
    }

    //Getters and Setters

    public ArrayList<File> getFiles() {
        return files;
    }

    public ConnectionEndpoint getConnectionEndpoint() {
        return connectionEndpoint;
    }
}
