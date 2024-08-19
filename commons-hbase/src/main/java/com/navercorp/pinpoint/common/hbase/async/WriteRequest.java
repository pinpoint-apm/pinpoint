package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.util.FutureUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class WriteRequest {

    private final TableName tableName;
    private final List<Put> puts;
    private final List<CompletableFuture<Void>> futures;

    public WriteRequest(TableName tableName, List<Put> puts) {
        this.tableName = Objects.requireNonNull(tableName, "tableName");
        this.puts = Objects.requireNonNull(puts, "puts");

        this.futures = FutureUtils.newFutureList(CompletableFuture::new, puts.size());
    }

    public TableName getTableName() {
        return tableName;
    }

    public List<Put> getPuts() {
        return puts;
    }

    public List<CompletableFuture<Void>> getFutures() {
        return futures;
    }

    public int size() {
        return puts.size();
    }
}
