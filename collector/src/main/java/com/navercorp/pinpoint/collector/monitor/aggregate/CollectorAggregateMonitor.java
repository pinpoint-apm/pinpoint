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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
public class CollectorAggregateMonitor {

    private static final String REPORTER_LOGGER_NAME = "com.navercorp.pinpoint.collector.AggregateReport";

    private final Logger logger = LoggerFactory.getLogger(REPORTER_LOGGER_NAME);

    private final AggregateReporter aggregateReporter;

    public CollectorAggregateMonitor(List<AggregateDataSource> aggregateDataSources) {
        Objects.requireNonNull(aggregateDataSources, "aggregateDataSources must not be null");
        this.aggregateReporter = new Slf4jAggregateReporter("aggregate-reporter", aggregateDataSources, logger);
    }

    @PostConstruct
    public void init() {
        aggregateReporter.start(60, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        aggregateReporter.stop();
    }
}
