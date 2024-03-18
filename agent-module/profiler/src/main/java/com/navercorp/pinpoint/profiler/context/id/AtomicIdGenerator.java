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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class AtomicIdGenerator implements IdGenerator {
    // TODO might be a good idea to refactor these into SamplingType

    // Reserved negative space (0 ~ -1000)
    public static final long UNTRACKED_ID = 0L;
    public static final long RESERVED_MAX = 0L;
    public static final long RESERVED_MIN = -1000;

    // Positive value for sampled new traces
    public static final long INITIAL_TRANSACTION_ID = 1L;
    // Negative value for sampled continuations, and unsampled new traces/continuations
    public static final long INITIAL_CONTINUED_TRANSACTION_ID = RESERVED_MIN - 1; // -1001
    public static final long INITIAL_DISABLED_ID = RESERVED_MIN - 2; // -1002
    public static final long INITIAL_CONTINUED_DISABLED_ID = RESERVED_MIN - 3; // -1003
    public static final long INITIAL_SKIPPED_ID = RESERVED_MIN - 4; // -1004
    public static final long INITIAL_CONTINUED_SKIPPED_ID = RESERVED_MIN - 5; // -1005

    public static final int DECREMENT_CYCLE = 5;
    public static final int NEGATIVE_DECREMENT_CYCLE = DECREMENT_CYCLE * -1;

    // Unique id for tracing a internal stacktrace and calculating a slow time of activethreadcount
    // moved here in order to make codes simpler for now
    // id generator for sampled new traces
    private final AtomicLong transactionId = new AtomicLong(INITIAL_TRANSACTION_ID);
    // id generator for sampled continued traces
    private final AtomicLong continuedTransactionId = new AtomicLong(INITIAL_CONTINUED_TRANSACTION_ID);
    // id generator for unsampled new traces
    private final AtomicLong disabledId = new AtomicLong(INITIAL_DISABLED_ID);
    // id generator for unsampled continued traces
    private final AtomicLong continuedDisabledId = new AtomicLong(INITIAL_CONTINUED_DISABLED_ID);
    // id generator for skipped new traces
    private final AtomicLong skippedId = new AtomicLong(INITIAL_SKIPPED_ID);
    // id generator for skipped continued traces
    private final AtomicLong continuedSkippedId = new AtomicLong(INITIAL_CONTINUED_SKIPPED_ID);

    @Inject
    public AtomicIdGenerator() {
    }

    @Override
    public long nextTransactionId() {
        return this.transactionId.getAndIncrement();
    }

    @Override
    public long nextContinuedTransactionId() {
        return this.continuedTransactionId.getAndAdd(NEGATIVE_DECREMENT_CYCLE);
    }

    @Override
    public long nextDisabledId() {
        return this.disabledId.getAndAdd(NEGATIVE_DECREMENT_CYCLE);
    }

    @Override
    public long nextContinuedDisabledId() {
        return this.continuedDisabledId.getAndAdd(NEGATIVE_DECREMENT_CYCLE);
    }

    @Override
    public long nextSkippedId() {
        return this.skippedId.getAndAdd(NEGATIVE_DECREMENT_CYCLE);
    }

    @Override
    public long nextContinuedSkippedId() {
        return this.continuedSkippedId.getAndAdd(NEGATIVE_DECREMENT_CYCLE);
    }

    @Override
    public long currentTransactionId() {
        return this.transactionId.get();
    }

    @Override
    public long currentContinuedTransactionId() {
        return this.continuedTransactionId.get();
    }

    @Override
    public long currentDisabledId() {
        return this.disabledId.get();
    }

    @Override
    public long currentContinuedDisabledId() {
        return this.continuedDisabledId.get();
    }

    @Override
    public long currentSkippedId() {
        return this.skippedId.get();
    }

    @Override
    public long currentContinuedSkippedId() {
        return this.continuedSkippedId.get();
    }
}