/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.stat.response;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.monitor.collector.response.DefaultResponseTimeMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.response.ResponseTimeMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.response.UnsupportedResponseTimeMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;

/**
 * @author Taejin Koo
 */
public class ResponseTimeMetricCollectorProvider implements Provider<ResponseTimeMetricCollector> {

    private final ResponseTimeMetric responseTimeMetric;

    @Inject
    public ResponseTimeMetricCollectorProvider(ResponseTimeMetric responseTimeMetric) {
        if (responseTimeMetric == null) {
            throw new NullPointerException("responseTimeMetric must not be null");
        }
        this.responseTimeMetric = responseTimeMetric;
    }

    @Override
    public ResponseTimeMetricCollector get() {
        if (responseTimeMetric == ResponseTimeMetric.UNSUPPORTED_RESPONSE_TIME_METRIC) {
            return new UnsupportedResponseTimeMetricCollector();
        }
        return new DefaultResponseTimeMetricCollector(responseTimeMetric);
    }

}
