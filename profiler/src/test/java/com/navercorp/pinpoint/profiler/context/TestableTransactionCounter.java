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

/**
 * @author HyunGil Jeong
 */
public class TestableTransactionCounter implements TransactionCounter {

    private long sampledTransactionCount = 0L;
    private long unsampledTransactionCount = 0L;
    private long sampledContinuationCount = 0L;
    private long unsampledContinuationCount = 0L;

    @Override
    public long getTotalTransactionCount() {
        return this.sampledTransactionCount + this.unsampledTransactionCount
                + this.sampledContinuationCount + this.unsampledContinuationCount;
    }
    
    public void addTransactionCount(SamplingType samplingType, long count) {
        switch (samplingType) {
        case SAMPLED_NEW:
            this.sampledTransactionCount += count;
            break;
        case UNSAMPLED_NEW:
            this.unsampledTransactionCount += count;
            break;
        case SAMPLED_CONTINUATION:
            this.sampledContinuationCount += count;
            break;
        case UNSAMPLED_CONTINUATION:
            this.unsampledContinuationCount += count;
            break;
        }
    }

    @Override
    public long getTransactionCount(SamplingType samplingType) {
        switch (samplingType) {
        case SAMPLED_NEW:
            return this.sampledTransactionCount;
        case UNSAMPLED_NEW:
            return this.unsampledTransactionCount;
        case SAMPLED_CONTINUATION:
            return this.sampledContinuationCount;
        case UNSAMPLED_CONTINUATION:
            return this.unsampledContinuationCount;
        default:
            return 0L;
        }
    }
}
