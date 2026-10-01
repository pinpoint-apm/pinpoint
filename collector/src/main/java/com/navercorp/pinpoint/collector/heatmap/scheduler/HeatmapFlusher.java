/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.scheduler;

import com.navercorp.pinpoint.collector.heatmap.counter.HeatmapCounter;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class HeatmapFlusher<K> {

    private final Logger logger = LogManager.getLogger(getClass());
    private final ThrottledLogger statLogger = ThrottledLogger.getUncountedIntervalLogger(logger, Duration.ofMinutes(5));
    private final ThrottledLogger abortLogger = ThrottledLogger.getUncountedIntervalLogger(logger, Duration.ofSeconds(10));
    private final ThrottledLogger failureLogger = ThrottledLogger.getUncountedIntervalLogger(logger, Duration.ofSeconds(10));

    private final HeatmapCounter<K> counter;
    private final BiFunction<K, Long, CompletableFuture<?>> sink;
    private final TaskScheduler scheduler;
    private final Duration flushInterval;
    private final long flushTimeoutNanos;

    public HeatmapFlusher(HeatmapCounter<K> counter, BiFunction<K, Long, CompletableFuture<?>> sink, TaskScheduler scheduler,
                          Duration flushInterval, Duration flushTimeout) {
        this.counter = Objects.requireNonNull(counter, "counter");
        this.sink = Objects.requireNonNull(sink, "sink");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.flushInterval = Objects.requireNonNull(flushInterval, "flushInterval");
        this.flushTimeoutNanos = toTimeoutNanos(Objects.requireNonNull(flushTimeout, "flushTimeout"));
    }

    // non-positive disables the flush timeout
    private static long toTimeoutNanos(Duration timeout) {
        if (timeout.isZero() || timeout.isNegative()) {
            return Long.MAX_VALUE;
        }
        return timeout.toNanos();
    }

    @PostConstruct
    public void scheduling() {
        logger.info("start heatmap flusher. interval:{}", flushInterval);
        // random initial delay for collectors started together
        long jitterMillis = ThreadLocalRandom.current().nextLong(flushInterval.toMillis());
        Instant startTime = Instant.now().plusMillis(jitterMillis);
        this.scheduler.scheduleWithFixedDelay(this::flush, startTime, flushInterval);
    }

    @PreDestroy
    public void shutdown() {
        flush();
    }

    public void flush() {
        Map<K, Long> snapshot = counter.snapshotAndClear();
        if (snapshot.isEmpty()) {
            return;
        }
        statLogger.info("flush heatmap stat. keys:{}", snapshot.size());
        // bound the flush and drop the rest, checked between sends so it may overrun by one max.block.ms
        long start = System.nanoTime();
        int attempted = 0;
        for (Map.Entry<K, Long> entry : snapshot.entrySet()) {
            send(entry.getKey(), entry.getValue());
            attempted++;
            long elapsedNanos = System.nanoTime() - start;
            if (elapsedNanos > flushTimeoutNanos) {
                abortLogger.warn("abort heatmap flush. elapsed:{}ms attempted:{} snapshot.size:{}",
                        TimeUnit.NANOSECONDS.toMillis(elapsedNanos), attempted, snapshot.size());
                return;
            }
        }
    }

    private void send(K key, long count) {
        try {
            sink.apply(key, count).whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logFailure(key, throwable);
                }
            });
        } catch (Exception e) {
            logFailure(key, e);
        }
    }

    private void logFailure(K key, Throwable throwable) {
        failureLogger.warn("failed to send heatmap stat. key:{}", key, throwable);
    }
}
