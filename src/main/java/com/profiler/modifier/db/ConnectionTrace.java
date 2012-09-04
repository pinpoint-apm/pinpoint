package com.profiler.modifier.db;

import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionTrace {

    public ConcurrentMap<Connection, String> connectionMap = new ConcurrentHashMap<Connection, String>();

    private static ConnectionTrace CONNECTION_TRACE = new ConnectionTrace();

    public static ConnectionTrace getConnectionTrace() {
        return CONNECTION_TRACE;
    }

    public void createConnection(Connection connection, String url) {
        this.connectionMap.put(connection, url);
    }


    public void closeConnection(Connection connection) {
        this.connectionMap.remove(connection);
    }

    public Set<Connection> getConnectionList() {
        return connectionMap.keySet();
    }
}
