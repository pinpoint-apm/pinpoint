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
import com.navercorp.pinpoint.profiler.monitor.codahale.AgentStatCollector;
import com.navercorp.pinpoint.profiler.monitor.codahale.MetricMonitorValues;
import com.navercorp.pinpoint.profiler.monitor.codahale.tps.metric.TransactionMetricSet;
import com.navercorp.pinpoint.thrift.dto.TTransaction;

/**
 * @author HyunGil Jeong
 */
public class TransactionMetricCollector implements AgentStatCollector<TTransaction> {
    
    public static final int UNSUPPORTED_TPS_METRIC = -1;
    private final Gauge<Integer> tpsGauge;

    @SuppressWarnings("unchecked")
    public TransactionMetricCollector(TransactionMetricSet transactionMetricSet) {
        if (transactionMetricSet == null) {
            throw new NullPointerException("tpsMetricSet must not be null");
        }
        Map<String, Metric> metrics = transactionMetricSet.getMetrics();
        this.tpsGauge = (Gauge<Integer>)MetricMonitorValues.getMetric(metrics, TRANSACTION_PER_SECOND, new EmptyGauge<Integer>(UNSUPPORTED_TPS_METRIC));
    }

    @Override
    public TTransaction collect() {
        TTransaction transaction = new TTransaction();
        transaction.setTps(this.tpsGauge.getValue());
        return transaction;
    }
}
