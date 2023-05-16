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

package com.navercorp.pinpoint.profiler.monitor.metric.response;

import com.google.inject.Inject;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Taejin Koo
 */
public class ReuseResponseTimeCollector implements ResponseTimeCollector {

    private volatile ResponseTimeCollector currentResponseTimeCollector;

    @Inject
    public ReuseResponseTimeCollector() {
        this.currentResponseTimeCollector = new ResponseTimeCollector();
    }

    @Override
    public void add(long value) {
        this.currentResponseTimeCollector.add(value);
    }

    @Override
    public ResponseTimeValue resetAndGetValue() {
        final ResponseTimeCollector reset = reset();

        final long totalValue = reset.getTotalValue();
        final long maxValue = reset.getMaxValue();
        final long transactionCount = reset.getTransactionCount();
        return new ResponseTimeValue0(totalValue, maxValue, transactionCount);
    }

    private ResponseTimeCollector reset() {
        final ResponseTimeCollector newValue = new ResponseTimeCollector();
        final ResponseTimeCollector copy = this.currentResponseTimeCollector;
        this.currentResponseTimeCollector = newValue;
        return copy;
    }

    private static class ResponseTimeCollector {
        private final LongAdder totalValue;
        private final LongAdder transactionCount;
        private final AtomicLong maxValue = new AtomicLong(0);

        private ResponseTimeCollector() {
            this.totalValue = new LongAdder();
            this.transactionCount = new LongAdder();
        }

        void add(long value) {
            transactionCount.increment();
            totalValue.add(value);

            boolean success = setMaxValue(value);
            while (!success) {
                success = setMaxValue(value);
            }
        }

        private boolean setMaxValue(long value) {
            long currentMaxValue = maxValue.get();
            if (currentMaxValue < value) {
                return maxValue.compareAndSet(currentMaxValue, value);
            }
            return true;
        }

        public long getTotalValue() {
            return totalValue.longValue();
        }

        public long getMaxValue() {
            return maxValue.get();
        }

        public long getTransactionCount() {
            return transactionCount.longValue();
        }
    }

    private static class ResponseTimeValue0 implements ResponseTimeValue {

        private final long totalResponseTime;
        private final long maxResponseTime;
        private final long transactionCount;

        private ResponseTimeValue0(long totalResponseTime, long maxResponseTime, long transactionCount) {
            this.totalResponseTime = totalResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.transactionCount = transactionCount;
        }

        @Override
        public long getAvg() {
            if (transactionCount == 0) {
                return totalResponseTime;
            }

            return totalResponseTime / transactionCount;
        }

        @Override
        public long getMax() {
            return maxResponseTime;
        }

        @Override
        public long getTotal() {
            return totalResponseTime;
        }

        @Override
        public long getTransactionCount() {
            return transactionCount;
        }

        @Override
        public String toString() {
            return "ResponseTimeValue0{" +
                    "totalResponseTime=" + totalResponseTime +
                    ", maxResponseTime=" + maxResponseTime +
                    ", transactionCount=" + transactionCount +
                    '}';
        }
    }

}
