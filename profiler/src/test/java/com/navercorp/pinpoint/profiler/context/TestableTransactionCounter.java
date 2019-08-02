/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.profiler.context.id.TransactionCounter;

/**
 * @author HyunGil Jeong
 */
public class TestableTransactionCounter implements TransactionCounter {

    private long sampledTransactionCount = 0L;
    private long unsampledTransactionCount = 0L;
    private long sampledContinuationCount = 0L;
    private long unsampledContinuationCount = 0L;
    private long skippedTransactionCount = 0L;
    private long skippedContinuationCount = 0L;

    @Override
    public long getTotalTransactionCount() {
        return this.sampledTransactionCount + this.unsampledTransactionCount
                + this.sampledContinuationCount + this.unsampledContinuationCount;
    }
    
    public void addSampledNewCount(long count) {
        this.sampledTransactionCount += count;
    }

    public void addSampledContinuationCount(long count) {
        this.sampledContinuationCount += count;
    }

    public void addUnSampledNewCount(long count) {
        this.unsampledTransactionCount += count;
    }

    public void addUnSampledContinuationCount(long count) {
        this.unsampledContinuationCount += count;
    }

    public void addSkippedNewCount(long count) {
        this.skippedTransactionCount += count;
    }

    public void addSkippedContinuationCount(long count) {
        this.skippedContinuationCount += count;
    }

    @Override
    public long getSampledNewCount() {
        return sampledTransactionCount;
    }

    @Override
    public long getSampledContinuationCount() {
        return sampledContinuationCount;
    }

    @Override
    public long getUnSampledNewCount() {
        return unsampledTransactionCount;
    }

    @Override
    public long getUnSampledContinuationCount() {
        return unsampledContinuationCount;
    }

    @Override
    public long getSkippedNewCount() {
        return skippedTransactionCount;
    }

    @Override
    public long getSkippedContinuationCount() {
        return skippedContinuationCount;
    }
}