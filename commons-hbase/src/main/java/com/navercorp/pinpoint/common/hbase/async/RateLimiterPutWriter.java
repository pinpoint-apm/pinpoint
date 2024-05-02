package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.RequestNotPermittedException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class RateLimiterPutWriter implements HbasePutWriter {
    private final HbasePutWriter putWriter;
    private final LimiterHelper limiter;
    private final BiConsumer<Void, Throwable> release;

    public RateLimiterPutWriter(HbasePutWriter putWriter, LimiterHelper limiter) {
        this.putWriter = Objects.requireNonNull(putWriter, "putWriter");
        this.limiter = Objects.requireNonNull(limiter, "rateLimiterFuture");
        this.release = this.limiter.release();
    }

    @Override
    public CompletableFuture<Void> put(TableName tableName, Put put) {
        acquire(1, tableName);
        boolean success = false;
        try {
            final CompletableFuture<Void> result = this.putWriter.put(tableName, put);
            if (result != null) {
                result.whenComplete(release);
                success = true;
            }
            return result;
        } finally {
            if (!success) {
                this.limiter.release(1);
            }
        }
    }

    @Override
    public List<CompletableFuture<Void>> put(TableName tableName, List<Put> puts) {
        final int size = puts.size();
        acquire(size, tableName);
        boolean success = false;
        try {
            final List<CompletableFuture<Void>> results = this.putWriter.put(tableName, puts);
            if (results != null) {
                for (CompletableFuture<Void> result : results) {
                    result.whenComplete(release);
                }
                success = true;
            }
            return results;
        } finally {
            if (!success) {
                this.limiter.release(size);
            }
        }
    }

    private void acquire(int size, TableName tableName) {
        if (!this.limiter.acquire(size)) {
            throw new RequestNotPermittedException("max concurrent requests reached. table:" + tableName, false);
        }
    }

    @Override
    public String toString() {
        return "RateLimiterPutWriter{" +
                "putWriter=" + putWriter +
                ", limiter=" + limiter +
                '}';
    }
}
