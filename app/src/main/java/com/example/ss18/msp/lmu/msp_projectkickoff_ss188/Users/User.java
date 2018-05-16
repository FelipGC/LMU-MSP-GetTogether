package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Users;

/**
 * Represents a user using this software. A user can either be a spectator or a presenter.
 * Spectator & Presenter inherit this class.
 */
public abstract class User {
    /**
     * Switches the role, i.e from presenter -> to spectator or the other way round
     */
    public abstract void changeRole();

    private String userName;

    public enum UserRole{SPECTATOR, PRESENTER};
    /**
     * The type the user is
     */
    protected UserRole roleType;

    //Getters & Setters

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UserRole getRoleType() {
        return roleType;
    }
}
