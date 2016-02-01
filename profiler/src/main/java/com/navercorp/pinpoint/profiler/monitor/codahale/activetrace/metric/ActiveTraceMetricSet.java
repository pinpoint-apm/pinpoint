/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale.activetrace.metric;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogramFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHistogramFactory.ActiveTraceHistogram;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.navercorp.pinpoint.thrift.dto.TActiveTraceHistogram;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class ActiveTraceMetricSet implements MetricSet {

    private final Gauge<TActiveTraceHistogram> activeTraceGauge;

    public ActiveTraceMetricSet(ActiveTraceLocator activeTraceLocator) {
        if (activeTraceLocator == null) {
            throw new NullPointerException("activeTraceLocator must not be null");
        }
        this.activeTraceGauge = new ActiveTraceHistogramGauge(activeTraceLocator);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        gauges.put(MetricMonitorValues.ACTIVE_TRACE_COUNT, this.activeTraceGauge);
        return gauges;
    }

    @Override
    public String toString() {
        return "Default ActiveTraceCountMetricSet";
    }

    private static class ActiveTraceHistogramGauge implements Gauge<TActiveTraceHistogram> {

        private final ActiveTraceHistogramFactory activeTraceHistogramFactory;

        private ActiveTraceHistogramGauge(ActiveTraceLocator activeTraceLocator) {
            this.activeTraceHistogramFactory = new ActiveTraceHistogramFactory(activeTraceLocator);
        }

        @Override
        public TActiveTraceHistogram getValue() {
            ActiveTraceHistogram activeTraceHistogram = this.activeTraceHistogramFactory.createHistogram();
            TActiveTraceHistogram tActiveTraceHistogram = new TActiveTraceHistogram();
            tActiveTraceHistogram.setHistogramSchemaType(activeTraceHistogram.getHistogramSchema().getTypeCode());
            tActiveTraceHistogram.setActiveTraceCount(activeTraceHistogram.getActiveTraceCounts());
            return tActiveTraceHistogram;
        }
    }

}
