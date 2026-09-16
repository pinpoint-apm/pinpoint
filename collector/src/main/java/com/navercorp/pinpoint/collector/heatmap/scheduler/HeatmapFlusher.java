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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

public class HeatmapFlusher<K> {

    private final Logger logger = LogManager.getLogger(getClass());
    private final ThrottledLogger statLogger = ThrottledLogger.getUncountedIntervalLogger(logger, Duration.ofMinutes(5));
    private final ThrottledLogger failureLogger = ThrottledLogger.getIntervalLogger(logger, Duration.ofSeconds(10));

    private final String name;
    private final HeatmapCounter<K> counter;
    private final BiConsumer<K, Long> sink;
    private final TaskScheduler scheduler;
    private final Duration flushInterval;

    public HeatmapFlusher(String name, HeatmapCounter<K> counter, BiConsumer<K, Long> sink, TaskScheduler scheduler, Duration flushInterval) {
        this.name = Objects.requireNonNull(name, "name");
        this.counter = Objects.requireNonNull(counter, "counter");
        this.sink = Objects.requireNonNull(sink, "sink");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.flushInterval = Objects.requireNonNull(flushInterval, "flushInterval");
    }

    @PostConstruct
    public void scheduling() {
        logger.info("start {} heatmap flusher. interval:{}", name, flushInterval);
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
        statLogger.info("flush {} heatmap stat. keys:{}", name, snapshot.size());
        for (Map.Entry<K, Long> entry : snapshot.entrySet()) {
            send(entry.getKey(), entry.getValue());
        }
    }

    private void send(K key, long count) {
        try {
            sink.accept(key, count);
        } catch (Exception e) {
            failureLogger.warn("failed to send {} heatmap stat. key:{}", name, key, e);
        }
    }
}
