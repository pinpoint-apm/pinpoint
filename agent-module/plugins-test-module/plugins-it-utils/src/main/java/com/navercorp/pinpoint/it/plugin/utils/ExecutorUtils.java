package com.navercorp.pinpoint.it.plugin.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


public final class ExecutorUtils {
    private ExecutorUtils() {
    }


    /**
     * Copy from <a href="https://github.com/google/guava">google/guava</a>
     */
    public static boolean shutdownAndAwaitTermination(
            ExecutorService service, long timeout, TimeUnit unit) {
        long halfTimeoutNanos = unit.toNanos(timeout) / 2;
        // Disable new tasks from being submitted
        service.shutdown();
        try {
            // Wait for half the duration of the timeout for existing tasks to terminate
            if (!service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS)) {
                // Cancel currently executing tasks
                service.shutdownNow();
                // Wait the other half of the timeout for tasks to respond to being cancelled
                service.awaitTermination(halfTimeoutNanos, TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException ie) {
            // Preserve interrupt status
            Thread.currentThread().interrupt();
            // (Re-)Cancel if current thread also interrupted
            service.shutdownNow();
        }
        return service.isTerminated();
    }

}
