package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase;

import com.google.android.gms.nearby.connection.Payload;

import java.util.HashMap;

/**
 * A singleton class simulating a data base to store all kinds of necessary information. The data base is
 * stored locally on the android device.
 */
public final class LocalDataBase {

    private LocalDataBase() {}

    private static String userName = "Unknown user";

    /**
     * Stores payloads we received during an active session (gets cleared after app exits/closes)
     */
    public final static HashMap<Long,Payload> receivedPayLoadData = new HashMap<Long,Payload>();
    /**
     * Stores payloads we sent during an active session (gets cleared after app exits/closes)
     */
    public final static HashMap<Long,Payload> sentPayLoadData = new HashMap<Long,Payload>();

    public static String getUserName() {return userName;}

    public static void setUserName(String userNameNew) {userName = userNameNew;}

}
