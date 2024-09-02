package com.navercorp.pinpoint.common.hbase.async;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.io.Closeable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AsyncPollingPutWriter implements HbasePutWriter, Closeable {

    private final AsyncPollerThread[] pollers;


    public AsyncPollingPutWriter(String name, TableWriterFactory factory, AsyncPollerOption option) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(factory, "factory");
        Objects.requireNonNull(option, "option");

        this.pollers = newAsyncWriteExecutors(name, factory, option);
    }

    @SuppressWarnings("resource")
    private AsyncPollerThread[] newAsyncWriteExecutors(String name, TableWriterFactory writerFactory, AsyncPollerOption option) {
        final AsyncPollerThread[] pollers =  new AsyncPollerThread[option.getParallelism()];
        for (int i = 0; i < pollers.length; i++) {
            pollers[i] = new AsyncPollerThread(name + i, writerFactory, option);
        }
        return pollers;
    }

    @Override
    public CompletableFuture<Void> put(TableName tableName, Put put) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(put, "put");

        AsyncPollerThread writer = getExecutor(tableName, put);

        List<CompletableFuture<Void>> futures = writer.write(tableName, List.of(put));
        return futures.get(0);
    }

    private AsyncPollerThread getExecutor(TableName tableName, Put put) {
        final int mod = mod(tableName.hashCode(), put.getRow());
        return pollers[mod];
    }

    int mod(int hbaseCode, byte[] row) {
        int hashcode = hbaseCode + Arrays.hashCode(row);
        return Math.floorMod(hashcode, pollers.length);
    }

    @Override
    public List<CompletableFuture<Void>> put(TableName tableName, List<Put> puts) {
        Objects.requireNonNull(tableName, "tableName");
        Objects.requireNonNull(puts, "puts");
        if (puts.isEmpty()) {
            return List.of(CompletableFuture.completedFuture(null));
        }

        Put put = puts.get(0);
        AsyncPollerThread writer = getExecutor(tableName, put);

        return writer.write(tableName, puts);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(pollers);
    }

    @Override
    public String toString() {
        return "AsyncPollingPutWriter{" +
                "parallelism=" + pollers.length +
                ", " + pollers[0] +
                '}';
    }
}
