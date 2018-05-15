package com.example.ss18.msp.lmu.msp_projectkickoff_ss188.Connection;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;

/**
 * Stores everything we need related to the NearbyConnection process.
 */
public class ConnectionDataBase {

    private final String TAG = "ConnectionDataBase";
    private static final ConnectionDataBase connectionDataBase = new ConnectionDataBase();

    public static ConnectionDataBase getInstance() {
        return connectionDataBase;
    }

    private ConnectionDataBase(){} //( Due to Singleton)

    /**
     * Currently discovered devices near us.
     */
    private final HashMap<String, ConnectionEndpoint> discoveredEndpoints = new HashMap<>();

    /**
     * All devices we are currently connected to.
     */
    private final HashMap<String, ConnectionEndpoint> establishedConnections = new HashMap<>();

    /** Represents a device we can connect to. */
    private static class ConnectionEndpoint {
        @NonNull
        private final String id; //Should be unique!
        @NonNull
        private final String name;

        private ConnectionEndpoint(@NonNull String id, @NonNull String name) {
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

    /**
     * Starts advertising to be spotted by discoverers
     */
    public void startAdvertising(){
        Log.i(TAG,"Starting advertising...");
        //TODO: Implement
    }

    /**
     * Stops the advertising process
     */
    public void stopAdvertising(){
        Log.i(TAG,"Stopping advertising!");
        //TODO: Implement
    }

    /**
     * Start the process of detecting nearby devices (connectors)
     */
    public void startDiscovering(){
        Log.i(TAG,"Starting discovering...");
        //TODO: Implement
    }
}
