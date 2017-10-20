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

package com.navercorp.pinpoint.profiler.monitor.metric.transaction;

import com.navercorp.pinpoint.profiler.context.id.TransactionCounter;

/**
 * @author HyunGil Jeong
 */
public class DefaultTransactionMetric implements TransactionMetric {

    private final TransactionGauge sampledNewGauge;
    private final TransactionGauge sampledContinuationGauge;
    private final TransactionGauge unsampledNewGauge;
    private final TransactionGauge unsampledContinuationGauge;

    public DefaultTransactionMetric(final TransactionCounter transactionCounter) {
        if (transactionCounter == null) {
            throw new NullPointerException("transactionCounter must not be null");
        }
        sampledNewGauge = TransactionGauge.from(new LongCounter() {
            @Override
            public long getCount() {
                return transactionCounter.getSampledNewCount();
            }
        });
        sampledContinuationGauge = TransactionGauge.from(new LongCounter() {
            @Override
            public long getCount() {
                return transactionCounter.getSampledContinuationCount();
            }
        });
        unsampledNewGauge = TransactionGauge.from(new LongCounter() {
            @Override
            public long getCount() {
                return transactionCounter.getUnSampledNewCount();
            }
        });
        unsampledContinuationGauge = TransactionGauge.from(new LongCounter() {
            @Override
            public long getCount() {
                return transactionCounter.getUnSampledContinuationCount();
            }
        });
    }

    @Override
    public TransactionMetricSnapshot getSnapshot() {
        long sampledNewCount = sampledNewGauge.getTransactionCount();
        long sampledContinuationCount = sampledContinuationGauge.getTransactionCount();
        long unsampledNewCount = unsampledNewGauge.getTransactionCount();
        long unsampledContinuationCount = unsampledContinuationGauge.getTransactionCount();
        return new TransactionMetricSnapshot(sampledNewCount, sampledContinuationCount, unsampledNewCount, unsampledContinuationCount);
    }

    @Override
    public String toString() {
        return "Default TransactionMetric";
    }

    private interface LongCounter {
        long getCount();
    }

    private static class TransactionGauge {
        private static final long UNINITIALIZED = -1L;

        private long prevTransactionCount = UNINITIALIZED;
        private final LongCounter longCounter;

        static TransactionGauge from(LongCounter longCounter) {
            return new TransactionGauge(longCounter);
        }

        private TransactionGauge(LongCounter longCounter) {
            if (longCounter == null) {
                throw new NullPointerException("longGauge must not be null");
            }
            this.longCounter = longCounter;
        }

        private final long getTransactionCount() {
            final long transactionCount = longCounter.getCount();
            if (transactionCount < 0) {
                return UNCOLLECTED;
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
