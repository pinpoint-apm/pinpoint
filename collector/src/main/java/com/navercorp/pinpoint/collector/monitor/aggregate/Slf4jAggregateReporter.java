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

import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class Slf4jAggregateReporter extends AggregateReporter {

    private final Logger logger;
    private final AggregateReportFormatter<?> formatter;

    public Slf4jAggregateReporter(String name, List<AggregateDataSource> aggregateDataSources, Logger logger) {
        this(name, aggregateDataSources, logger, AggregateReportFormatter.DEFAULT_STRING);
    }

    public Slf4jAggregateReporter(String name, List<AggregateDataSource> aggregateDataSources, Logger logger, AggregateReportFormatter<?> formatter) {
        super(name, aggregateDataSources);
        this.logger = Objects.requireNonNull(logger, "logger must not be null");
        this.formatter = Objects.requireNonNull(formatter, "formatter must not be null");
    }

    @Override
    protected void report(AggregateReport aggregateReport) {
        if (logger.isInfoEnabled()) {
            logger.info("{}", formatter.format(aggregateReport));
        }
    }
}
