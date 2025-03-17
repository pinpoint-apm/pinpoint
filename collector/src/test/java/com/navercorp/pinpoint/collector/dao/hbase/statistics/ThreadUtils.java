package com.navercorp.pinpoint.collector.dao.hbase.statistics;

import org.junit.jupiter.api.Assertions;

public final class ThreadUtils {

    public static void awaitTermination(Thread thread, long millis){
        try {
            thread.join(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for thread to join", e);
        }
        Assertions.assertFalse(thread.isAlive(), "Timeout waiting for flusherLatch");
    }

}
