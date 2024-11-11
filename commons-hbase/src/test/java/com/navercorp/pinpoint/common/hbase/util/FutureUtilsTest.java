package com.navercorp.pinpoint.common.hbase.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class  FutureUtilsTest {

    @Test
    void allOf() {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("future1");
        CompletableFuture<String> future2 = CompletableFuture.completedFuture("future2");

        List<String> sync = FutureUtils.allOf(Arrays.asList(future1, future2));

        assertEquals(2, sync.size());
        assertEquals("future1", sync.get(0));
        assertEquals("future2", sync.get(1));
    }

    @Test
    void allOfAsync() throws Exception {
        CompletableFuture<String> future1 = CompletableFuture.completedFuture("future1");
        CompletableFuture<String> future2 = new CompletableFuture<>();

        CompletableFuture<List<String>> async = FutureUtils.allOfAsync(Arrays.asList(future1, future2));

        future2.complete("future2");

        List<String> result = async.get(100, TimeUnit.MILLISECONDS);

        assertEquals(2, result.size());
        assertEquals("future1", result.get(0));
        assertEquals("future2", result.get(1));
    }

}