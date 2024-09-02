package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.util.IOUtils;
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
        IOUtils.closeQuietly(connection, (ioe) -> logger.warn("Failed to close connection", ioe));
    }

    @Override
    public String toString() {
        return "SimpleConnectionSelector{" +
                "connection=" + connection +
                '}';
    }
}
