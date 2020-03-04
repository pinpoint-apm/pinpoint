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

package com.navercorp.pinpoint.profiler.context.provider.stat.transaction;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.UnsupportedMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.transaction.DefaultTransactionMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;

/**
 * @author HyunGil Jeong
 */
public class TransactionMetricCollectorProvider implements Provider<AgentStatMetricCollector<TransactionMetricSnapshot>> {

    private final TransactionMetric transactionMetric;

    @Inject
    public TransactionMetricCollectorProvider(TransactionMetric transactionMetric) {
        this.transactionMetric = Assert.requireNonNull(transactionMetric, "transactionMetric");
    }

    @Override
    public AgentStatMetricCollector<TransactionMetricSnapshot> get() {
        if (transactionMetric == TransactionMetric.UNSUPPORTED_TRANSACTION_METRIC) {
            return new UnsupportedMetricCollector<TransactionMetricSnapshot>();
        }
        return new DefaultTransactionMetricCollector(transactionMetric);
    }
}
