/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.strategy;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.ArithmeticOperation;
import com.navercorp.pinpoint.common.server.bo.codec.TypedBufferHandler;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.impl.DeltaEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.impl.DeltaOfDeltaEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.impl.RepeatCountEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.impl.ValueEncodingStrategy;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public enum UnsignedLongEncodingStrategy implements EncodingStrategy<Long> {
    NONE(new ValueEncodingStrategy.Unsigned<Long>(TypedBufferHandler.LONG_BUFFER_HANDLER)),
    REPEAT_COUNT(new RepeatCountEncodingStrategy.Unsigned<Long>(TypedBufferHandler.LONG_BUFFER_HANDLER)),
    DELTA(new DeltaEncodingStrategy.Unsigned<Long>(TypedBufferHandler.LONG_BUFFER_HANDLER, ArithmeticOperation.LONG_OPERATIONS)),
    DELTA_OF_DELTA(new DeltaOfDeltaEncodingStrategy.Unsigned<Long>(TypedBufferHandler.LONG_BUFFER_HANDLER, ArithmeticOperation.LONG_OPERATIONS));

    private final EncodingStrategy<Long> delegate;

    private static final Set<UnsignedLongEncodingStrategy> UNSIGNED_LONG_ENCODING_STRATEGY = EnumSet.allOf(UnsignedLongEncodingStrategy.class);

    UnsignedLongEncodingStrategy(EncodingStrategy<Long> delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte getCode() {
        return this.delegate.getCode();
    }

    @Override
    public void encodeValues(Buffer buffer, List<Long> values) {
        this.delegate.encodeValues(buffer, values);
    }

    @Override
    public List<Long> decodeValues(Buffer buffer, int numValues) {
        return this.delegate.decodeValues(buffer, numValues);
    }

    public static UnsignedLongEncodingStrategy getFromCode(int code) {

        for (UnsignedLongEncodingStrategy encodingStrategy : UNSIGNED_LONG_ENCODING_STRATEGY) {
            if (encodingStrategy.getCode() == (code & 0xFF)) {
                return encodingStrategy;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static class Analyzer implements StrategyAnalyzer<Long> {

        private final EncodingStrategy<Long> bestStrategy;
        private final List<Long> values;

        private Analyzer(EncodingStrategy<Long> bestStrategy, List<Long> values) {
            this.bestStrategy = bestStrategy;
            this.values = values;
        }

        @Override
        public EncodingStrategy<Long> getBestStrategy() {
            return this.bestStrategy;
        }

        @Override
        public List<Long> getValues() {
            return this.values;
        }

        public static class Builder implements StrategyAnalyzerBuilder<Long> {

            private final List<Long> values = new ArrayList<Long>();
            private long previousValue = 0L;
            private long previousDelta = 0L;

            private int byteSizeValue = 0;
            private int byteSizeDelta = 0;
            private int byteSizeDeltaOfDelta = 0;
            private int byteSizeRepeatCount = 0;

            private int repeatedValueCount = 0;

            @Override
            public StrategyAnalyzerBuilder<Long> addValue(Long value) {
                long delta = value - this.previousValue;
                if (this.values.isEmpty()) {
                    initializeByteSizes(value);
                } else {
                    updateByteSizes(value, delta);
                    this.previousDelta = delta;
                }
                this.previousValue = value;

                this.values.add(value);
                return this;
            }

            @Override
            public StrategyAnalyzer<Long> build() {
                if (this.repeatedValueCount > 0) {
                    this.byteSizeRepeatCount += BytesUtils.computeVar32Size(this.repeatedValueCount);
                }
                EncodingStrategy<Long> bestStrategy;
                int minimumNumBytesUsed = Collections.min(Arrays.asList(
                        this.byteSizeValue,
                        this.byteSizeDelta,
                        this.byteSizeDeltaOfDelta,
                        this.byteSizeRepeatCount));
                if (this.byteSizeValue == minimumNumBytesUsed) {
                    bestStrategy = NONE;
                } else if (this.byteSizeDelta == minimumNumBytesUsed) {
                    bestStrategy = DELTA;
                } else if (this.byteSizeDeltaOfDelta == minimumNumBytesUsed) {
                    bestStrategy = DELTA_OF_DELTA;
                } else {
                    bestStrategy = REPEAT_COUNT;
                }
                List<Long> values = new ArrayList<Long>(this.values);
                this.values.clear();
                return new Analyzer(bestStrategy, values);
            }

            int getByteSizeValue() {
                return byteSizeValue;
            }

            int getByteSizeDelta() {
                return byteSizeDelta;
            }

            int getByteSizeDeltaOfDelta() {
                return byteSizeDeltaOfDelta;
            }

            int getByteSizeRepeatCount() {
                return byteSizeRepeatCount;
            }

            private void initializeByteSizes(long value) {
                int expectedNumBytesUsedByValue = expectedBytesVLength(value);
                this.byteSizeValue = expectedNumBytesUsedByValue;
                this.byteSizeDelta = expectedNumBytesUsedByValue;
                this.byteSizeDeltaOfDelta = expectedNumBytesUsedByValue;
                this.repeatedValueCount = 1;
                this.byteSizeRepeatCount = expectedNumBytesUsedByValue;
            }

            private void updateByteSizes(long value, long delta) {
                int expectedNumBytesUsedByValue = expectedBytesVLength(value);
                this.byteSizeValue += expectedNumBytesUsedByValue;
                this.byteSizeDelta += expectedBytesVLength(value ^ this.previousValue);
                this.byteSizeDeltaOfDelta += expectedBytesSVLength(delta - this.previousDelta);
                if (this.previousValue != value) {
                    this.byteSizeRepeatCount += BytesUtils.computeVar32Size(this.repeatedValueCount);
                    this.byteSizeRepeatCount += expectedNumBytesUsedByValue;
                    this.repeatedValueCount = 1;
                } else {
                    this.repeatedValueCount++;
                }
            }

            private int expectedBytesVLength(long value) {
                return BytesUtils.computeVar64Size(value);
            }

            private int expectedBytesSVLength(long value) {
                return expectedBytesVLength(BytesUtils.longToZigZag(value));
            }
        }
    }
}
