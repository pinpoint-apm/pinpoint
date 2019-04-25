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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class AggregateReport {

    private final List<AggregateDataSource> aggregateDataSources;

    private AggregateReport(List<AggregateDataSource> aggregateDataSources) {
        this.aggregateDataSources = Objects.requireNonNull(aggregateDataSources, "aggregateDataSources must not be null");
    }

    public static AggregateReport create(List<AggregateDataSource> aggregateDataSources) {
        if (aggregateDataSources == null) {
            return new AggregateReport(Collections.emptyList());
        }
        return new AggregateReport(Collections.unmodifiableList(aggregateDataSources));
    }

    public List<AggregateDataSource> getAggregateDataSources() {
        return aggregateDataSources;
    }
}
