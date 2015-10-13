/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.codahale.tps.metric;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import com.navercorp.pinpoint.profiler.context.TransactionCounter;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;

/**
 * @author HyunGil Jeong
 */
public class TransactionMetricSet implements MetricSet {

    private final Gauge<Integer> tpsGauge;

    public TransactionMetricSet(TransactionCounter transactionCounter) {
        if (transactionCounter == null) {
            throw new NullPointerException("transactionCounter must not be null");
        }
        this.tpsGauge = new TpsGauge(transactionCounter);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        gauges.put(MetricMonitorValues.TRANSACTION_PER_SECOND, this.tpsGauge);
        return Collections.unmodifiableMap(gauges);
    }

    @Override
    public String toString() {
        return "Default TransactionMetricSet";
    }

    private class TpsGauge implements Gauge<Integer> {

        private static final long UNINITIALIZED = -1L;

        private final TransactionCounter transactionCounter;

        private long lastTickMs = UNINITIALIZED;
        private long lastTransactionCount = UNINITIALIZED;

        private TpsGauge(TransactionCounter transactionCounter) {
            this.transactionCounter = transactionCounter;
        }

        @Override
        public Integer getValue() {
            final long currentTickMs = System.currentTimeMillis();
            final long transactionCount = transactionCounter.getTotalTransactionCount();
            if (this.lastTickMs == UNINITIALIZED) {
                this.lastTickMs = currentTickMs;
                this.lastTransactionCount = transactionCount;
                return 0;
            }
            final long timeMsSinceLastTick = currentTickMs - this.lastTickMs;
            final long transactionCountSinceLastTick = transactionCount - this.lastTransactionCount;

            this.lastTickMs = currentTickMs;
            this.lastTransactionCount = transactionCount;

            return calculateTps(transactionCountSinceLastTick, timeMsSinceLastTick);
        }

        private int calculateTps(long count, long timeMs) {
            if (count <= 0 || timeMs <= 0) {
                return 0;
            }
            // ignore improbable overflow
            final long tps = (timeMs + (count * 1000) - 1) / timeMs;
            if (tps > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else {
                return (int)tps;
            }
        }

    }

}
