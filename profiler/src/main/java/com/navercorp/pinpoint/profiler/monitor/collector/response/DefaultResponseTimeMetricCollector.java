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

package com.navercorp.pinpoint.profiler.monitor.collector.response;

import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeValue;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import com.navercorp.pinpoint.thrift.dto.TResponseTime;

/**
 * @author Taejin Koo
 */
public class DefaultResponseTimeMetricCollector implements ResponseTimeMetricCollector {

    private final ResponseTimeMetric responseTimeMetric;

    public DefaultResponseTimeMetricCollector(ResponseTimeMetric responseTimeMetric) {
        if (responseTimeMetric == null) {
            throw new NullPointerException("responseTimeMetric must not be null");
        }
        this.responseTimeMetric = responseTimeMetric;
    }

    @Override
    public TResponseTime collect() {
        ResponseTimeValue responseTimeValue = responseTimeMetric.responseTimeValue();
        long avg = responseTimeValue.getAvg();

        TResponseTime tResponseTime = new TResponseTime();
        if (avg != 0) {
            tResponseTime.setAvg(avg);
        }
        return tResponseTime;
    }

}
