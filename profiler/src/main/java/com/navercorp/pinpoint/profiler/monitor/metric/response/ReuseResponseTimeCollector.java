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

/**
 * @author Taejin Koo
 */
public class ReuseResponseTimeCollector {

    private volatile ResponseTimeCollector currentResponseTimeCollector;

    public ReuseResponseTimeCollector() {
        this.currentResponseTimeCollector = new ResponseTimeCollector();
    }

    public void add(long value) {
        this.currentResponseTimeCollector.add(value);
    }

    public ResponseTimeValue resetAndGetValue() {
        ResponseTimeValue result = new ResponseTimeValue0(currentResponseTimeCollector.getTotalValue());
        this.currentResponseTimeCollector = new ResponseTimeCollector();

        return result;
    }

    private static class ResponseTimeCollector {
        private final LongAdder totalValue;

        private ResponseTimeCollector() {
            this.totalValue = new LongAdder();
        }

        void add(long value) {
            totalValue.add(value);
        }

        public long getTotalValue() {
            return totalValue.longValue();
        }

    }

    private static class ResponseTimeValue0 implements ResponseTimeValue {

        private final long totalResponseTime;

        private ResponseTimeValue0(long totalResponseTime) {
            this.totalResponseTime = totalResponseTime;
        }

        @Override
        public long getTotalResponseTime() {
            return totalResponseTime;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("TotalResponseTimeValue0{");
            sb.append("totalResponseTime=").append(totalResponseTime);
            sb.append('}');
            return sb.toString();
        }

    }

}
