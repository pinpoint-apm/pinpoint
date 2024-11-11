package com.navercorp.pinpoint.common.hbase.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public final class FutureUtils {
    private FutureUtils() {
    }

    public static <V> List<CompletableFuture<V>> newFutureList(Supplier<CompletableFuture<V>> supplier, int size) {
        List<CompletableFuture<V>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    public static <V> void addListener(final CompletableFuture<V> future, final CompletableFuture<V> action) {
        future.whenComplete((v, t) -> {
            if (t != null) {
                t = unwrapCompletionException(t);
                action.completeExceptionally(t);
            } else {
                action.complete(v);
            }
        });
    }

    public static Throwable unwrapCompletionException(Throwable error) {
        if (error instanceof CompletionException) {
            Throwable cause = error.getCause();
            if (cause != null) {
                return cause;
            }
        }

        return error;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> allOf(List<CompletableFuture<T>> futures) {
        Objects.requireNonNull(futures, "futures");

        CompletableFuture<T>[] futuresArray = (CompletableFuture<T>[]) futures.toArray(new CompletableFuture<?>[0]);
        return allOf(futuresArray);
    }

    public static <T> List<T> allOf(CompletableFuture<T>[] futures) {
        Objects.requireNonNull(futures, "futures");

        final List<T> result = new ArrayList<>(futures.length);
        for (CompletableFuture<T> future : futures) {
            result.add(future.join());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<List<T>> allOfAsync(List<CompletableFuture<T>> futures) {
        Objects.requireNonNull(futures, "futures");

        final CompletableFuture<T>[] futuresArray = (CompletableFuture<T>[]) futures.toArray(new CompletableFuture<?>[0]);

        return CompletableFuture.allOf(futuresArray)
                .thenApply(v -> allOf(futuresArray));
    }
}
