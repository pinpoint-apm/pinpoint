/*
 * Copyright 2014 NAVER Corp.
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

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class IdGenerator {

    // Positive value for sampled traces
    public static final long INITIAL_TRANSACTION_ID = 1L;
    public static final long INITIAL_CONTINUED_TRANSACTION_ID = Long.MAX_VALUE;
    // Negative value for unsampled traces
    public static final long INITIAL_DISABLED_ID = -1L;
    public static final long INITIAL_CONTINUED_DISABLED_ID = Long.MIN_VALUE;

    public static final long UNTRACKED_ID = 0L;

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

    public long nextTransactionId() {
        return this.transactionId.getAndIncrement();
    }

    public long nextContinuedTransactionId() {
        return this.continuedTransactionId.getAndDecrement();
    }

    public long nextDisabledId() {
        return this.disabledId.getAndDecrement();
    }

    public long nextContinuedDisabledId() {
        return this.continuedDisabledId.getAndIncrement();
    }

    public long currentTransactionId() {
        return this.transactionId.get();
    }

    public long currentContinuedTransactionId() {
        return this.continuedTransactionId.get();
    }

    public long currentDisabledId() {
        return this.disabledId.get();
    }

    public long currentContinuedDisabledId() {
        return this.continuedDisabledId.get();
    }
}
