package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.DataBase;

/**
 * A singleton class simulating a data base to store all kinds of necessary information. The data base is
 * stored locally on the android device.
 */
public final class LocalDataBase {

    private LocalDataBase() {}

    private static String userName = "Unknown user";


    public static String getUserName() {return userName;}

    public static void setUserName(String userNameNew) {userName = userNameNew;}
}
