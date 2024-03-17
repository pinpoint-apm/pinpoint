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
public class TransactionMetricSnapshot {

    private final long sampledNewCount;
    private final long sampledContinuationCount;
    private final long unsampledNewCount;
    private final long unsampledContinuationCount;
    private final long skippedNewCount;
    private final long skippedContinuationCount;

    public TransactionMetricSnapshot(long sampledNewCount,
                                     long sampledContinuationCount,
                                     long unsampledNewCount,
                                     long unsampledContinuationCount,
                                     long skippedNewCount,
                                     long skippedContinuationCount) {
        this.sampledNewCount = sampledNewCount;
        this.sampledContinuationCount = sampledContinuationCount;
        this.unsampledNewCount = unsampledNewCount;
        this.unsampledContinuationCount = unsampledContinuationCount;
        this.skippedNewCount = skippedNewCount;
        this.skippedContinuationCount = skippedContinuationCount;
    }

    public long getSampledNewCount() {
        return sampledNewCount;
    }

    public long getSampledContinuationCount() {
        return sampledContinuationCount;
    }

    public long getUnsampledNewCount() {
        return unsampledNewCount;
    }

    public long getUnsampledContinuationCount() {
        return unsampledContinuationCount;
    }

    public long getSkippedNewCount() {
        return skippedNewCount;
    }

    public long getSkippedContinuationCount() {
        return skippedContinuationCount;
    }
}