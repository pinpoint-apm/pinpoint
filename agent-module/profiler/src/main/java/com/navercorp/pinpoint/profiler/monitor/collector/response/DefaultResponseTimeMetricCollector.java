/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.monitor.collector.response;

import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DefaultResponseTimeMetricCollector implements AgentStatMetricCollector<ResponseTimeValue> {

    private final ResponseTimeMetric responseTimeMetric;

    public DefaultResponseTimeMetricCollector(ResponseTimeMetric responseTimeMetric) {
        this.responseTimeMetric = Objects.requireNonNull(responseTimeMetric, "responseTimeMetric");
    }

    @Override
    public ResponseTimeValue collect() {
        final ResponseTimeValue responseTimeValue = responseTimeMetric.responseTimeValue();
        return responseTimeValue;
    }

}
