/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;

import java.lang.instrument.ClassFileTransformer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * A transformer registered by a class or package based matcher, together with its whole matcher condition.
 */
class IndexValue {
    private static final AtomicLongFieldUpdater<IndexValue> ACCUMULATOR_UPDATER
            = AtomicLongFieldUpdater.newUpdater(IndexValue.class, "accumulatorTimeMillis");

    private final MatcherOperand operand;
    private final ClassFileTransformer transformer;
    // updated through ACCUMULATOR_UPDATER: one long per index entry instead of an AtomicLong object
    private volatile long accumulatorTimeMillis;

    IndexValue(final MatcherOperand operand, final ClassFileTransformer transformer) {
        this.operand = Objects.requireNonNull(operand, "operand");
        this.transformer = Objects.requireNonNull(transformer, "transformer");
    }

    MatcherOperand getOperand() {
        return operand;
    }

    ClassFileTransformer getTransformer() {
        return transformer;
    }

    long getAccumulatorTimeMillis() {
        return accumulatorTimeMillis;
    }

    long accumulatorTime(final long startTimeMillis) {
        final long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
        ACCUMULATOR_UPDATER.addAndGet(this, elapsedTimeMillis);
        return elapsedTimeMillis;
    }

    @Override
    public String toString() {
        return "IndexValue{operand=" + operand + ", transformer=" + transformer + '}';
    }
}
