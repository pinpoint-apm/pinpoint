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
public class DefaultTransactionCounter implements TransactionCounter {

    private final IdGenerator idGenerator;

    public DefaultTransactionCounter(IdGenerator idGenerator) {
        if (idGenerator == null) {
            throw new NullPointerException("idGenerator cannot be null");
        }
        this.idGenerator = idGenerator;
    }
    
    @Override
    public long getTransactionCount(SamplingType samplingType) {
        // overflow improbable
        switch (samplingType) {
        case SAMPLED_NEW:
            return idGenerator.currentTransactionId() - IdGenerator.INITIAL_TRANSACTION_ID;
        case SAMPLED_CONTINUATION:
            return Math.abs(idGenerator.currentContinuedTransactionId() - IdGenerator.INITIAL_CONTINUED_TRANSACTION_ID) / IdGenerator.DECREMENT_CYCLE;
        case UNSAMPLED_NEW:
            return Math.abs(idGenerator.currentDisabledId() - IdGenerator.INITIAL_DISABLED_ID) / IdGenerator.DECREMENT_CYCLE;
        case UNSAMPLED_CONTINUATION:
            return Math.abs(idGenerator.currentContinuedDisabledId() - IdGenerator.INITIAL_CONTINUED_DISABLED_ID) / IdGenerator.DECREMENT_CYCLE;
        default:
            return 0L;
        }
    }

    @Override
    public long getTotalTransactionCount() {
        long count = 0L;
        for (SamplingType samplingType : SamplingType.values()) {
            count += getTransactionCount(samplingType);
        }
        return count;
    }
}
