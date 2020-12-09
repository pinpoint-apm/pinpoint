package com.navercorp.pinpoint.grpc.channelz;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.InternalInstrumented;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ChannelzUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChannelzUtils.class);

    private static final long timeout = 3000L;

    private ChannelzUtils() {
    }

    public static <T> T getResult(String name, InternalInstrumented<T> instrumented) {
        final ListenableFuture<T> future = instrumented.getStats();
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

    public static <T> List<T> getResults(String name, List<InternalInstrumented<T>> instrumentedList) {
        Assert.requireNonNull(instrumentedList, "instrumentedList");

        final List<ListenableFuture<T>> listenableFutures = new ArrayList<>(instrumentedList.size());
        for (InternalInstrumented<T> each : instrumentedList) {
            listenableFutures.add(each.getStats());
        }
        ListenableFuture<List<T>> listListenableFuture = Futures.allAsList(listenableFutures);
        try {
            return listListenableFuture.get(timeout, TimeUnit.MILLISECONDS);
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
