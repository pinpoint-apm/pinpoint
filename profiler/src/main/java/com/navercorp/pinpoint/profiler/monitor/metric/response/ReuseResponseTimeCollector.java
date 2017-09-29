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
import com.navercorp.pinpoint.profiler.util.jdk.LongAdder;

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
        final  long transactionCount = reset.getTransactionCount();
        ResponseTimeValue result = new ResponseTimeValue0(totalValue, transactionCount);
        return result;
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

        private ResponseTimeCollector() {
            this.totalValue = new LongAdder();
            this.transactionCount = new LongAdder();
        }

        void add(long value) {
            transactionCount.increment();
            totalValue.add(value);
        }

        public long getTotalValue() {
            return totalValue.longValue();
        }

        public long getTransactionCount() {
            return transactionCount.longValue();
        }

    }

    private static class ResponseTimeValue0 implements ResponseTimeValue {

        private final long totalResponseTime;
        private final long transactionCount;

        private ResponseTimeValue0(long totalResponseTime, long transactionCount) {
            this.totalResponseTime = totalResponseTime;
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
        public long getTotal() {
            return totalResponseTime;
        }

        @Override
        public long getTransactionCount() {
            return transactionCount;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ResponseTimeValue0{");
            sb.append("totalResponseTime=").append(totalResponseTime);
            sb.append(", transactionCount=").append(transactionCount);
            sb.append('}');
            return sb.toString();
        }

    }

}
