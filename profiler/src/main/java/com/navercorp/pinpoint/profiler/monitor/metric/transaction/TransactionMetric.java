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

/**
 * @author HyunGil Jeong
 */
public interface TransactionMetric {

    long UNCOLLECTED = -1L;

    TransactionMetric UNSUPPORTED_TRANSACTION_METRIC = new TransactionMetric() {

        private final TransactionMetricSnapshot uncollectedSnaphot = new TransactionMetricSnapshot(UNCOLLECTED, UNCOLLECTED, UNCOLLECTED, UNCOLLECTED);

        @Override
        public TransactionMetricSnapshot getSnapshot() {
            return uncollectedSnaphot;
        }

        @Override
        public String toString() {
            return "Unsupported TransactionMetric";
        }
    };

    TransactionMetricSnapshot getSnapshot();
}
