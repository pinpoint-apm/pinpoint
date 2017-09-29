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
public enum UnsignedIntegerEncodingStrategy implements EncodingStrategy<Integer> {
    NONE(new ValueEncodingStrategy.Unsigned<Integer>(TypedBufferHandler.INTEGER_BUFFER_HANDLER)),
    REPEAT_COUNT(new RepeatCountEncodingStrategy.Unsigned<Integer>(TypedBufferHandler.INTEGER_BUFFER_HANDLER)),
    DELTA(new DeltaEncodingStrategy.Unsigned<Integer>(TypedBufferHandler.INTEGER_BUFFER_HANDLER, ArithmeticOperation.INTEGER_OPERATIONS)),
    DELTA_OF_DELTA(new DeltaOfDeltaEncodingStrategy.Unsigned<Integer>(TypedBufferHandler.INTEGER_BUFFER_HANDLER, ArithmeticOperation.INTEGER_OPERATIONS)), ;

    private final EncodingStrategy<Integer> delegate;
    private static final Set<UnsignedIntegerEncodingStrategy> UNSIGNED_INTEGER_ENCODING_STRATEGY = EnumSet.allOf(UnsignedIntegerEncodingStrategy.class);

    UnsignedIntegerEncodingStrategy(EncodingStrategy<Integer> delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte getCode() {
        return this.delegate.getCode();
    }

    @Override
    public void encodeValues(Buffer buffer, List<Integer> values) {
        this.delegate.encodeValues(buffer, values);
    }

    @Override
    public List<Integer> decodeValues(Buffer buffer, int numValues) {
        return this.delegate.decodeValues(buffer, numValues);
    }

    public static UnsignedIntegerEncodingStrategy getFromCode(int code) {

        for (UnsignedIntegerEncodingStrategy encodingStrategy : UNSIGNED_INTEGER_ENCODING_STRATEGY) {
            if (encodingStrategy.getCode() == (code & 0xFF)) {
                return encodingStrategy;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static class Analyzer implements StrategyAnalyzer<Integer> {

        private final EncodingStrategy<Integer> bestStrategy;
        private final List<Integer> values;

        private Analyzer(EncodingStrategy<Integer> bestStrategy, List<Integer> values) {
            this.bestStrategy = bestStrategy;
            this.values = values;
        }

        @Override
        public EncodingStrategy<Integer> getBestStrategy() {
            return this.bestStrategy;
        }

        @Override
        public List<Integer> getValues() {
            return this.values;
        }

        public static class Builder implements StrategyAnalyzerBuilder<Integer> {

            private final List<Integer> values = new ArrayList<Integer>();
            private int previousValue = 0;
            private int previousDelta = 0;

            private int byteSizeValue = 0;
            private int byteSizeDelta = 0;
            private int byteSizeDeltaOfDelta = 0;
            private int byteSizeRepeatCount = 0;

            private int repeatedValueCount = 0;

            @Override
            public StrategyAnalyzerBuilder<Integer> addValue(Integer value) {
                int delta = value - this.previousValue;
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
            public StrategyAnalyzer<Integer> build() {
                if (this.repeatedValueCount > 0) {
                    this.byteSizeRepeatCount += BytesUtils.computeVar32Size(this.repeatedValueCount);
                }
                EncodingStrategy<Integer> bestStrategy;
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
                List<Integer> values = new ArrayList<Integer>(this.values);
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

            private void initializeByteSizes(int value) {
                int expectedNumBytesUsedByValue = expectedBytesVLength(value);
                this.byteSizeValue = expectedNumBytesUsedByValue;
                this.byteSizeDelta = expectedNumBytesUsedByValue;
                this.byteSizeDeltaOfDelta = expectedNumBytesUsedByValue;
                this.repeatedValueCount = 1;
                this.byteSizeRepeatCount = expectedNumBytesUsedByValue;
            }

            private void updateByteSizes(int value, int delta) {
                int expectedNumBytesUsedByValue = expectedBytesVLength(value);
                this.byteSizeValue += expectedNumBytesUsedByValue;
                this.byteSizeDelta += expectedBytesVLength(value ^ this.previousValue);
                this.byteSizeDeltaOfDelta += expectedBytesSVLength(delta - this.previousDelta);
                if (this.previousValue != value) {
                    this.byteSizeRepeatCount += expectedBytesVLength(this.repeatedValueCount);
                    this.byteSizeRepeatCount += expectedNumBytesUsedByValue;
                    this.repeatedValueCount = 1;
                } else {
                    this.repeatedValueCount++;
                }
            }

            private int expectedBytesVLength(int value) {
                if (value < 0) {
                    return BytesUtils.computeVar64Size(value);
                } else {
                    return BytesUtils.computeVar32Size(value);
                }
            }

            private int expectedBytesSVLength(int value) {
                return BytesUtils.computeVar32Size(BytesUtils.intToZigZag(value));
            }
        }
    }
}
