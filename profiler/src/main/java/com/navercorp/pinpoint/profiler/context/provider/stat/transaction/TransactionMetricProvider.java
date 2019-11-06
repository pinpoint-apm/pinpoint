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

package com.navercorp.pinpoint.profiler.context.provider.stat.transaction;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.id.TransactionCounter;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.DefaultTransactionMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetric;

/**
 * @author HyunGil Jeong
 */
public class TransactionMetricProvider implements Provider<TransactionMetric> {

    private final TransactionCounter transactionCounter;

    @Inject
    public TransactionMetricProvider(TransactionCounter transactionCounter) {
        this.transactionCounter = transactionCounter;
    }

    @Override
    public TransactionMetric get() {
        if (transactionCounter == null) {
            return TransactionMetric.UNSUPPORTED_TRANSACTION_METRIC;
        }
        return new DefaultTransactionMetric(transactionCounter);
    }
}
