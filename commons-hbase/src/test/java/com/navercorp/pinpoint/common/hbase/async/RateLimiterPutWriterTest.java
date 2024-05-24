package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.hbase.RequestNotPermittedException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterPutWriterTest {

    TableName tableName = TableName.valueOf("test");

    @Mock
    HbasePutWriter putWriter;

    @Test
    void put() {
        when(putWriter.put(any(TableName.class), any(Put.class)))
                .thenReturn(new CompletableFuture<>());

        ConcurrencyLimiterHelper helper = new ConcurrencyLimiterHelper(1);
        RateLimiterPutWriter writer = new RateLimiterPutWriter(putWriter, helper);

        writer.put(tableName, new Put(new byte[10]));
        Assertions.assertThrows(RequestNotPermittedException.class, () -> {
            writer.put(tableName, new Put(new byte[10]));
        });

        Assertions.assertEquals(1, helper.count());

    }

    @Test
    void putN() {
        when(putWriter.put(any(TableName.class), any(List.class)))
                .thenReturn(List.of(new CompletableFuture<>(), new CompletableFuture<>()));

        ConcurrencyLimiterHelper helper = new ConcurrencyLimiterHelper(2);
        RateLimiterPutWriter writer = new RateLimiterPutWriter(putWriter, helper);

        Put put = new Put(new byte[10]);
        writer.put(tableName, List.of(put, put));
        Assertions.assertThrows(RequestNotPermittedException.class, () -> {
            writer.put(tableName, List.of(put, put));
        });
    }

    @Test
    void putN_complete() {
        when(putWriter.put(any(TableName.class), any(List.class)))
                .thenReturn(List.of(new CompletableFuture<>(), new CompletableFuture<>()));

        ConcurrencyLimiterHelper helper = new ConcurrencyLimiterHelper(2);
        RateLimiterPutWriter writer = new RateLimiterPutWriter(putWriter, helper);

        Put put = new Put(new byte[10]);
        List<CompletableFuture<Void>> results = writer.put(tableName, List.of(put, put));
        results.forEach(future -> future.complete(null));

        writer.put(tableName, List.of(put, put));
    }

    @Test
    void put_error() {
        when(putWriter.put(any(TableName.class), any(Put.class)))
                .thenThrow(new RuntimeException("error"));

        ConcurrencyLimiterHelper helper = new ConcurrencyLimiterHelper(1);
        RateLimiterPutWriter writer = new RateLimiterPutWriter(putWriter, helper);

        Assertions.assertThrows(RuntimeException.class, () -> {
            writer.put(tableName, new Put(new byte[10]));
        });
        assertEquals(0, helper.count());
    }


    @Test
    void put_acquire() {
        when(putWriter.put(any(TableName.class), any(Put.class)))
                .then((Answer<CompletableFuture<Void>>) invocation -> new CompletableFuture<>());

        ConcurrencyLimiterHelper helper = new ConcurrencyLimiterHelper(1);
        RateLimiterPutWriter writer = new RateLimiterPutWriter(putWriter, helper);

        CompletableFuture<Void> future = writer.put(tableName, new Put(new byte[10]));
        Assertions.assertThrows(RuntimeException.class, () -> {
            writer.put(tableName, new Put(new byte[10]));
        });
        assertEquals(1, helper.count());
        future.complete(null);
        assertEquals(0, helper.count());

    }
}