package com.navercorp.pinpoint.common.hbase.async;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TableWriterFactory {
    Writer writer(TableName tableName);


    interface Writer {
        List<CompletableFuture<Void>> put(List<Put> put);
    }
}
