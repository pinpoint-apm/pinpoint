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

package com.navercorp.pinpoint.profiler.monitor.codahale.tps;

import static com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues.*;

import java.util.Map;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.navercorp.pinpoint.profiler.monitor.codahale.tps.metric.TransactionMetricSet;
import com.navercorp.pinpoint.thrift.dto.TTransaction;

/**
 * @author HyunGil Jeong
 */
public class DefaultTransactionMetricCollector implements TransactionMetricCollector {

    private static final long UNSUPPORTED_TRANSACTION_METRIC = -1;
    private static final Gauge<Long> UNSUPPORTED_GAUGE = new EmptyGauge<Long>(UNSUPPORTED_TRANSACTION_METRIC);

    private final Gauge<Long> sampledNewGauge;
    private final Gauge<Long> sampledContinuationGauge;
    private final Gauge<Long> unsampledNewGauge;
    private final Gauge<Long> unsampledContinuationGuage;

    @SuppressWarnings("unchecked")
    public DefaultTransactionMetricCollector(TransactionMetricSet transactionMetricSet) {
        if (transactionMetricSet == null) {
            throw new NullPointerException("transactionMetricSet must not be null");
        }
        Map<String, Metric> metrics = transactionMetricSet.getMetrics();
        this.sampledNewGauge = (Gauge<Long>) MetricMonitorValues.getMetric(metrics, TRANSACTION_SAMPLED_NEW, UNSUPPORTED_GAUGE);
        this.sampledContinuationGauge = (Gauge<Long>) MetricMonitorValues.getMetric(metrics, TRANSACTION_SAMPLED_CONTINUATION, UNSUPPORTED_GAUGE);
        this.unsampledNewGauge = (Gauge<Long>) MetricMonitorValues.getMetric(metrics, TRANSACTION_UNSAMPLED_NEW, UNSUPPORTED_GAUGE);
        this.unsampledContinuationGuage = (Gauge<Long>) MetricMonitorValues.getMetric(metrics, TRANSACTION_UNSAMPLED_CONTINUATION, UNSUPPORTED_GAUGE);
    }

    @Override
    public TTransaction collect() {
        TTransaction transaction = new TTransaction();
        transaction.setSampledNewCount(this.sampledNewGauge.getValue());
        transaction.setSampledContinuationCount(this.sampledContinuationGauge.getValue());
        transaction.setUnsampledNewCount(this.unsampledNewGauge.getValue());
        transaction.setUnsampledContinuationCount(this.unsampledContinuationGuage.getValue());
        return transaction;
    }
}
