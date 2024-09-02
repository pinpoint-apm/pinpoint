package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.AsyncConnection;
import org.apache.hadoop.hbase.client.AsyncTable;

import java.util.Objects;

public class AsyncTableWriterSelectorFactory implements TableWriterFactory {

    private final ConnectionSelector selector;

    public AsyncTableWriterSelectorFactory(ConnectionSelector selector) {
        this.selector = Objects.requireNonNull(selector, "selector");
    }

    @Override
    public Writer writer(TableName tableName) {
        AsyncConnection connection = selector.getConnection();
        final AsyncTable<?> table = connection.getTable(tableName);
        return table::put;
    }

    @Override
    public String toString() {
        return "AsyncTableWriterSelectorFactory{" +
                "selector=" + selector +
                '}';
    }
}
