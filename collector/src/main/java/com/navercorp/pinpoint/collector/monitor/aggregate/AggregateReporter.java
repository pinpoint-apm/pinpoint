/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor.aggregate;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author HyunGil Jeong
 */
public abstract class AggregateReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregateReporter.class);

    private final List<AggregateDataSource> aggregateDataSources;
    private final ScheduledExecutorService executor;

    protected AggregateReporter(String name, List<AggregateDataSource> aggregateDataSources) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(aggregateDataSources, "aggregateDataSources must not be null");
        this.aggregateDataSources = Collections.unmodifiableList(aggregateDataSources);
        this.executor = Executors.newSingleThreadScheduledExecutor(new PinpointThreadFactory("pinpoint-" + name + "-reporter", true));
    }

    public void start(long period, TimeUnit timeUnit) {
        executor.scheduleAtFixedRate(() -> {
            try {
                report0(aggregateDataSources);
            } catch (RuntimeException e) {
                LOGGER.error("RuntimeException thrown from {}", getClass().getSimpleName(), e);
            }
        }, period, period, timeUnit);
    }

    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    LOGGER.error("{} ScheduledExecutorService failed to terminate", getClass().getSimpleName());
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void report0(List<AggregateDataSource> aggregateDataSources) {
        List<AggregateDataSource> snapshots = aggregateDataSources.stream()
                .filter(Objects::nonNull)
                .map(AggregateDataSourceSnapshot::new)
                .collect(Collectors.toList());
        report(AggregateReport.create(snapshots));
    }

    protected abstract void report(AggregateReport aggregateReport);

    private static class AggregateDataSourceSnapshot implements AggregateDataSource {
        private final String name;
        private final List<Aggregate> aggregates;

        private AggregateDataSourceSnapshot(AggregateDataSource aggregateDataSource) {
            this.name = aggregateDataSource.getName();
            List<Aggregate> aggregates = aggregateDataSource.getAggregates();
            if (aggregates == null) {
                aggregates = Collections.emptyList();
            }
            this.aggregates = aggregates;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<Aggregate> getAggregates() {
            return aggregates;
        }
    }
}
