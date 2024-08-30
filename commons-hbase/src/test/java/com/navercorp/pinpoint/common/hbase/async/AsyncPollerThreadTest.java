package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.util.FutureUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Row;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


class AsyncPollerThreadTest {
    private final Random random = new Random();
    TableName table = TableName.valueOf("table");

    @Test
//    @RepeatedTest(5)
    void write() throws Exception {
        TableWriterFactory factory = tableName -> this::putAll;

        AsyncPollerOption option = new AsyncPollerOption();
        option.setQueueSize(100);
        option.setWriteBufferSize(100);
        option.setWriteBufferPeriodicFlush(200);

        AsyncPollerThread poller = new AsyncPollerThread("test", factory, option);

        Put put1 = new Put(nextBytes(8));
        Put put2 = new Put(nextBytes(9));
        Put put3 = new Put(nextBytes(10));

        List<CompletableFuture<Void>> future1 = poller.write(this.table, List.of(put1));
        List<CompletableFuture<Void>> future2 = poller.write(this.table, List.of(put2, put3));


        Assertions.assertNull(awaitAndGet(future1, 0));

        Assertions.assertNull(awaitAndGet(future2, 0));
        Assertions.assertNull(awaitAndGet(future2, 1));

        List<CompletableFuture<Void>> future4 = poller.write(this.table, List.of(put1));
        Assertions.assertNull(awaitAndGet(future4, 0));

        poller.close();
    }

    private byte[] nextBytes(int size) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    private Void awaitAndGet(List<CompletableFuture<Void>> listFuture, int index) throws Exception {
        CompletableFuture<Void> future = listFuture.get(index);
        return future.get(1000, TimeUnit.MILLISECONDS);
    }

    private <T> List<CompletableFuture<T>> putAll(List<? extends Row> list) {
        return FutureUtils.newFutureList(() -> CompletableFuture.completedFuture(null), list.size());
    }
}