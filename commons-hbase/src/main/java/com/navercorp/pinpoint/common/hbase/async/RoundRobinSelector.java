package com.navercorp.pinpoint.common.hbase.async;

import org.apache.commons.lang3.AutoCloseables;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinSelector implements ConnectionSelector, Closeable {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AtomicInteger mod = new AtomicInteger(0);

    private final AsyncConnection[] connections;

    public RoundRobinSelector(List<AsyncConnection> connections) {
        Objects.requireNonNull(connections, "connections");
        this.connections = connections.toArray(new AsyncConnection[0]);
    }

    void setModKey(int mod) {
        this.mod.set(mod);
    }

    @Override
    public AsyncConnection getConnection() {
        final int index = getIndex();
        return connections[index];
    }

    private int getIndex() {
        final long next = mod.getAndIncrement();
        return Math.floorMod(next, connections.length);
    }

    @Override
    public void close() {
        logger.info("Closing connections {}", connections.length);
        for (AsyncConnection connection : connections) {
            AutoCloseables.closeQuietly(connection, (e) -> logger.warn("Failed to close connection", e));
        }
    }

    @Override
    public String toString() {
        return "RoundRobinSelector{" +
                "connections=" + Arrays.toString(connections) +
                '}';
    }
}
