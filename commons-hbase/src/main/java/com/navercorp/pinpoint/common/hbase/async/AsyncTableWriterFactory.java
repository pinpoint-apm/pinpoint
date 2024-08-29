package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.AsyncTable;

import java.util.Objects;

public class AsyncTableWriterFactory implements TableWriterFactory {
    private final AsyncConnection connection;

    public AsyncTableWriterFactory(AsyncConnection connection) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    @Override
    public Writer writer(TableName tableName) {
        final AsyncTable<?> table = connection.getTable(tableName);
        return table::put;
    }

    @Override
    public String toString() {
        return "AsyncTableWriterFactory{" +
                "connection=" + connection +
                '}';
    }
}
