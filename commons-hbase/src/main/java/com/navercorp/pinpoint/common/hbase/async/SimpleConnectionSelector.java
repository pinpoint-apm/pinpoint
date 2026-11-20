package com.navercorp.pinpoint.common.hbase.async;

import org.apache.commons.lang3.AutoCloseables;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.Objects;

public class SimpleConnectionSelector implements ConnectionSelector, Closeable {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AsyncConnection connection;

    public SimpleConnectionSelector(AsyncConnection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");

    }


    @Override
    public AsyncConnection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        AutoCloseables.closeQuietly(connection, (e) -> logger.warn("Failed to close connection", e));
    }

    @Override
    public String toString() {
        return "SimpleConnectionSelector{" +
                "connection=" + connection +
                '}';
    }
}
