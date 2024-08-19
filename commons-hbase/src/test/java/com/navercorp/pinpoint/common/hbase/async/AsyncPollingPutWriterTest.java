package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.util.FutureUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


class AsyncPollingPutWriterTest {

    TableWriterFactory factory = tableName -> this::putAll;
    TableName tableName = TableName.valueOf("table");

    @Test
    void put() {
        AsyncPollerOption option = new AsyncPollerOption();
        option.setParallelism(2);

        Put put = new Put(RandomUtils.nextBytes(8));

        AsyncPollingPutWriter writer = new AsyncPollingPutWriter("test", factory, option);
        CompletableFuture<Void> future = writer.put(tableName, put);

        Void join = future.join();
        Assertions.assertNull(join);

        writer.close();
    }

    @Test
    void putAll() throws Exception {
        AsyncPollerOption option = new AsyncPollerOption();
        option.setParallelism(2);

        Put put1 = new Put(RandomUtils.nextBytes(8));
        Put put2 = new Put(RandomUtils.nextBytes(8));

        AsyncPollingPutWriter writer = new AsyncPollingPutWriter("test", factory, option);
        List<CompletableFuture<Void>> futures = writer.put(tableName, List.of(put1, put2));

        Assertions.assertNull(awaitAndGet(futures, 0));
        Assertions.assertNull(awaitAndGet(futures, 1));

        writer.close();
    }

    private Void awaitAndGet(List<CompletableFuture<Void>> listFuture, int index) throws Exception {
        CompletableFuture<Void> future = listFuture.get(index);
        return future.get(1000, TimeUnit.MILLISECONDS);
    }


    private <T> List<CompletableFuture<T>> putAll(List<Put> list) {
        return FutureUtils.newFutureList(() -> CompletableFuture.completedFuture(null), list.size());
    }
}