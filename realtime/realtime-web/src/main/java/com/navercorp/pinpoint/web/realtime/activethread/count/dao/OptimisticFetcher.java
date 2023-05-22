/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.realtime.activethread.count.dao;

import com.navercorp.pinpoint.realtime.util.MinTermThrottle;
import com.navercorp.pinpoint.realtime.util.Throttle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Flux;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class OptimisticFetcher<T> implements Fetcher<T> {

    private static final Logger logger = LogManager.getLogger(OptimisticFetcher.class);

    private final Function<Integer, Flux<T>> valueSupplier;
    private final long recordMaxAgeNanos;
    private final long prepareTimeoutNanos;

    private final AtomicReference<Record<T>> recordRef = new AtomicReference<>();
    private final Throttle prepareThrottle = new MinTermThrottle(TimeUnit.SECONDS.toNanos(3));
    private final AtomicInteger numPrepared = new AtomicInteger(0);
    private final AtomicLong latestPrepareTime = new AtomicLong(0);

    public OptimisticFetcher(
            Function<Integer, Flux<T>> valueSupplier,
            long recordMaxAgeNanos,
            long prepareTimeoutNanos
    ) {
        this.valueSupplier = Objects.requireNonNull(valueSupplier, "valueSupplier");
        this.recordMaxAgeNanos = recordMaxAgeNanos;
        this.prepareTimeoutNanos = prepareTimeoutNanos;
    }

    @Override
    public T fetch() {
        final Record<T> latestRecord = this.recordRef.get();
        if (latestRecord == null || latestRecord.olderThan(System.nanoTime() - this.recordMaxAgeNanos)) {
            prepareForNext();
            return null;
        }

        if (this.latestPrepareTime.get() < System.nanoTime() - this.prepareTimeoutNanos) {
            prepareForNext();
        }

        return latestRecord.getValue();
    }

    private void prepareForNext() {
        if (this.prepareThrottle.hit()) {
            logger.debug("Fetcher Started");
            final long prepareTime = System.nanoTime();
            this.valueSupplier.apply(this.numPrepared.getAndIncrement())
                    .doOnNext(item -> logger.trace("Fetcher Received: {}", item))
                    .doOnComplete(() -> logger.debug("Fetcher Completed"))
                    .subscribe(this::put);
            this.latestPrepareTime.set(prepareTime);
        }
    }

    private void put(T supply) {
        if (supply == null) {
            return;
        }
        final Record<T> nextRecord = new Record<>(supply);
        this.recordRef.set(nextRecord);
    }

    private static final class Record<T> {

        private final T value;
        private final long createdAt;

        Record(T value) {
            this.value = value;
            this.createdAt = System.nanoTime();
        }

        T getValue() {
            return this.value;
        }

        boolean olderThan(long thresholdNanos) {
            return this.createdAt < thresholdNanos;
        }

    }

}
