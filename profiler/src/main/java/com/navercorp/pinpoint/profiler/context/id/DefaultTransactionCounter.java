/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.id;

import com.google.inject.Inject;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class DefaultTransactionCounter implements TransactionCounter {

    private final IdGenerator idGenerator;

    @Inject
    public DefaultTransactionCounter(IdGenerator idGenerator) {
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator");
    }
    
    @Override
    public long getSampledNewCount() {
        return idGenerator.currentTransactionId() - AtomicIdGenerator.INITIAL_TRANSACTION_ID;
    }

    @Override
    public long getSampledContinuationCount() {
        return Math.abs(idGenerator.currentContinuedTransactionId() - AtomicIdGenerator.INITIAL_CONTINUED_TRANSACTION_ID) / AtomicIdGenerator.DECREMENT_CYCLE;
    }

    @Override
    public long getUnSampledNewCount() {
        return Math.abs(idGenerator.currentDisabledId() - AtomicIdGenerator.INITIAL_DISABLED_ID) / AtomicIdGenerator.DECREMENT_CYCLE;
    }

    @Override
    public long getUnSampledContinuationCount() {
        return Math.abs(idGenerator.currentContinuedDisabledId() - AtomicIdGenerator.INITIAL_CONTINUED_DISABLED_ID) / AtomicIdGenerator.DECREMENT_CYCLE;
    }

    @Override
    public long getSkippedNewCount() {
        return Math.abs(idGenerator.currentSkippedId() - AtomicIdGenerator.INITIAL_SKIPPED_ID) / AtomicIdGenerator.DECREMENT_CYCLE;
    }

    @Override
    public long getSkippedContinuationCount() {
        return Math.abs(idGenerator.currentContinuedSkippedId() - AtomicIdGenerator.INITIAL_CONTINUED_SKIPPED_ID) / AtomicIdGenerator.DECREMENT_CYCLE;
    }

    @Override
    public long getTotalTransactionCount() {
        long count = getSampledNewCount();
        count += getSampledContinuationCount();
        count += getUnSampledNewCount();
        count += getUnSampledContinuationCount();
        count += getSkippedNewCount();
        count += getSkippedContinuationCount();
        return count;
    }
}