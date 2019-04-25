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

import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public interface AggregateReportFormatter<T> {

    T format(AggregateReport aggregateReport);

    AggregateReportFormatter<String> DEFAULT_STRING = new AggregateReportFormatter<String>() {

        final String DEFAULT_EMPTY_REPORT = "Empty aggregate report.";
        final String SEPARATOR = System.lineSeparator();

        @Override
        public String format(AggregateReport aggregateReport) {
            if (aggregateReport == null) {
                throw new NullPointerException("aggregateReport must not be null");
            }
            List<AggregateDataSource> aggregateDataSources = aggregateReport.getAggregateDataSources();
            if (CollectionUtils.isEmpty(aggregateDataSources)) {
                return DEFAULT_EMPTY_REPORT;
            }
            boolean isFirst = true;
            StringBuilder sb = new StringBuilder();
            for (AggregateDataSource aggregateDataSource : aggregateDataSources) {
                if (isFirst) {
                    sb.append(SEPARATOR);
                }
                isFirst = false;
                final String name = aggregateDataSource.getName();
                final List<Aggregate> aggregates = aggregateDataSource.getAggregates();
                sb.append(name).append(", Total : ").append(aggregates.size());
                for (Aggregate aggregate : aggregates) {
                    sb.append(SEPARATOR).append("  ").append(aggregate.getKey()).append(": ").append(aggregate.getValue());
                }
            }
            return sb.toString();
        }
    };
}
