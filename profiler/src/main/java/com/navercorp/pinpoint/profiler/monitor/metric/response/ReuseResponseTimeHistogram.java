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

import com.navercorp.pinpoint.profiler.util.jdk.LongAdder;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Taejin Koo
 */
public class ReuseResponseTimeHistogram {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock rLock = rwLock.readLock();
    private final Lock wLock = rwLock.writeLock();

    private volatile ResponseTimeHistogram currentHistogram;

    public ReuseResponseTimeHistogram() {
        this.currentHistogram = new ResponseTimeHistogram();
    }

    public void add(long value) {
        rLock.lock();
        try {
            this.currentHistogram.add(value);
        } finally {
            rLock.unlock();
        }
    }

    public ResponseTimeHistogramValue resetAndGetValue() {
        ResponseTimeHistogramValue result = null;
        wLock.lock();
        try {
            result = new ResponseTimeHistogramValue0(currentHistogram.getCount(), currentHistogram.getTotalValue());
            this.currentHistogram = new ResponseTimeHistogram();
        } finally {
            wLock.unlock();
        }

        return result;
    }

    private static class ResponseTimeHistogram {
        private final LongAdder count;
        private final LongAdder totalValue;

        private ResponseTimeHistogram() {
            this.count = new LongAdder();
            this.totalValue = new LongAdder();
        }

        void add(long value) {
            count.increment();
            totalValue.add(value);
        }

        public long getCount() {
            return count.longValue();
        }

        public long getTotalValue() {
            return totalValue.longValue();
        }

    }

    private static class ResponseTimeHistogramValue0 implements ResponseTimeHistogramValue {

        private final long count;
        private final long totalValue;

        private ResponseTimeHistogramValue0(long count, long totalValue) {
            this.count = count;
            this.totalValue = totalValue;
        }

        public long getCount() {
            return count;
        }

        public long getAvg() {
            if (count == 0) {
                return 0;
            }

            return totalValue / count;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ResponseTimeHistogramValue0{");
            sb.append("count=").append(count);
            sb.append(", totalValue=").append(totalValue);
            sb.append('}');
            return sb.toString();
        }

    }

}
