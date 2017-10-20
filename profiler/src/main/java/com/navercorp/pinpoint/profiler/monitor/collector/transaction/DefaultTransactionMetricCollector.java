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

package com.navercorp.pinpoint.profiler.monitor.collector.transaction;

import com.google.inject.Inject;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetricSnapshot;
import com.navercorp.pinpoint.thrift.dto.TTransaction;

/**
 * @author HyunGil Jeong
 */
public class DefaultTransactionMetricCollector implements TransactionMetricCollector {

    private final TransactionMetric transactionMetric;

    @Inject
    public DefaultTransactionMetricCollector(TransactionMetric transactionMetric) {
        this.transactionMetric = transactionMetric;
    }

    @Override
    public TTransaction collect() {
        TTransaction transaction = new TTransaction();
        TransactionMetricSnapshot transactionMetricSnapshot = transactionMetric.getSnapshot();
        transaction.setSampledNewCount(transactionMetricSnapshot.getSampledNewCount());
        transaction.setSampledContinuationCount(transactionMetricSnapshot.getSampledContinuationCount());
        transaction.setUnsampledNewCount(transactionMetricSnapshot.getUnsampledNewCount());
        transaction.setUnsampledContinuationCount(transactionMetricSnapshot.getUnsampledContinuationCount());
        return transaction;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultTransactionMetricCollector{");
        sb.append("transactionMetric=").append(transactionMetric);
        sb.append('}');
        return sb.toString();
    }
}
