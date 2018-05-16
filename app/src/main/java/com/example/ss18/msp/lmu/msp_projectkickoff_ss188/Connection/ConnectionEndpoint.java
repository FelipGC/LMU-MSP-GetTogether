package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.support.annotation.NonNull;

/**
 * Represents a device we can connect to.
 */
public final class ConnectionEndpoint {

    @NonNull
    private final String id; //Should be unique!
    @NonNull
    private final String name;

    public ConnectionEndpoint(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    //Getters
    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ConnectionEndpoint) {
            ConnectionEndpoint other = (ConnectionEndpoint) obj;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("ConnectionEndpoint{id=%s, name=%s}", id, name);
    }
}
