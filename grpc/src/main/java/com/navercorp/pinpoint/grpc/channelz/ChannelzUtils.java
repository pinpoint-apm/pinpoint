package com.navercorp.pinpoint.grpc.channelz;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.InternalInstrumented;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ChannelzUtils {
    private static final Logger logger = LogManager.getLogger(ChannelzUtils.class);

    private static final long timeout = 3000L;

    private ChannelzUtils() {
    }

    public static <T> T getResult(String name, InternalInstrumented<T> instrumented) {
        if (instrumented == null) {
            return null;
        }
        final ListenableFuture<T> future = instrumented.getStats();
        return unwrapFuture(name, future);
    }

    public static <T> List<T> getResults(String name, List<InternalInstrumented<T>> instrumentedList) {
        Objects.requireNonNull(instrumentedList, "instrumentedList");

        final List<ListenableFuture<T>> listenableFutures = new ArrayList<>(instrumentedList.size());
        for (InternalInstrumented<T> each : instrumentedList) {
            listenableFutures.add(each.getStats());
        }
        ListenableFuture<List<T>> listListenableFuture = Futures.allAsList(listenableFutures);
        return unwrapFuture(name, listListenableFuture);
    }

    private static <T> T unwrapFuture(String name, ListenableFuture<T> future) {
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.info("ExecutionError {} {}", name, e.getMessage());
        } catch (TimeoutException e) {
            logger.info("Timeout {} {}", name, e.getMessage());
        }
        return null;
    }

}
