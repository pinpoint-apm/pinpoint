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

    private final Gauge<Long> sampledNewGauge;
    private final Gauge<Long> sampledContinuationGauge;
    private final Gauge<Long> unsampledNewGauge;
    private final Gauge<Long> unsampledContinuationGauge;

    public TransactionMetricSet(final TransactionCounter transactionCounter) {
        if (transactionCounter == null) {
            throw new NullPointerException("transactionCounter must not be null");
        }
        this.sampledNewGauge = TransactionGauge.wrap(new LongGauge() {
            @Override
            public long getValue() {
                return transactionCounter.getSampledNewCount();
            }
        });
        this.sampledContinuationGauge = TransactionGauge.wrap(new LongGauge() {
            @Override
            public long getValue() {
                return transactionCounter.getSampledContinuationCount();
            }
        });
        this.unsampledNewGauge = TransactionGauge.wrap(new LongGauge() {
            @Override
            public long getValue() {
                return transactionCounter.getUnSampledNewCount();
            }
        });
        this.unsampledContinuationGauge = TransactionGauge.wrap(new LongGauge() {
            @Override
            public long getValue() {
                return transactionCounter.getUnSampledContinuationCount();
            }
        });
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> gauges = new HashMap<String, Metric>();
        gauges.put(MetricMonitorValues.TRANSACTION_SAMPLED_NEW, this.sampledNewGauge);
        gauges.put(MetricMonitorValues.TRANSACTION_SAMPLED_CONTINUATION, this.sampledContinuationGauge);
        gauges.put(MetricMonitorValues.TRANSACTION_UNSAMPLED_NEW, this.unsampledNewGauge);
        gauges.put(MetricMonitorValues.TRANSACTION_UNSAMPLED_CONTINUATION, this.unsampledContinuationGauge);
        return Collections.unmodifiableMap(gauges);
    }

    @Override
    public String toString() {
        return "Default TransactionMetricSet";
    }

    private interface LongGauge {
        long getValue();
    }

    private static class TransactionGauge implements Gauge<Long> {
        private static final long UNINITIALIZED = -1L;

        private long prevTransactionCount = UNINITIALIZED;
        private final LongGauge longGauge;

        static TransactionGauge wrap(LongGauge longGauge) {
            return new TransactionGauge(longGauge);
        }

        private TransactionGauge(LongGauge longGauge) {
            if (longGauge == null) {
                throw new NullPointerException("longGauge must not be null");
            }
            this.longGauge = longGauge;
        }

        @Override
        public final Long getValue() {
            final long transactionCount = longGauge.getValue();
            if (transactionCount < 0) {
                return 0L;
            }
            if (this.prevTransactionCount == UNINITIALIZED) {
                this.prevTransactionCount = transactionCount;
                return 0L;
            }
            final long transactionCountDelta = transactionCount - this.prevTransactionCount;
            this.prevTransactionCount = transactionCount;
            return transactionCountDelta;
        }

    }

}
