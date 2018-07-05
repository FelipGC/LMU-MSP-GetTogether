package de.lmu.msp.gettogether.Users;

import de.lmu.msp.gettogether.DataBase.LocalDataBase;

import java.io.Serializable;

/**
 * Represents a user using this software. A user can either be a spectator or a presenter.
 * Spectator & Presenter inherit this class.
 */
public abstract class User implements Serializable {
    /**
     * Switches the role, i.e from presenter -> to spectator or the other way round
     */
    public abstract void changeRole();

    public enum UserRole{SPECTATOR, PRESENTER}
    /**
     * The type the user is
     */
    protected UserRole roleType;

    //Getters & Setters

    public String getUserName() {
        return LocalDataBase.getUserName();
    }

    public UserRole getRoleType() {
        return roleType;
    }

}
