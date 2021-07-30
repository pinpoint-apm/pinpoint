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
import com.navercorp.pinpoint.common.server.bo.codec.TypedBufferHandler;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
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
public enum UnsignedShortEncodingStrategy implements EncodingStrategy<Short> {
    NONE(new ValueEncodingStrategy.Unsigned<>(TypedBufferHandler.SHORT_BUFFER_HANDLER)),
    REPEAT_COUNT(new RepeatCountEncodingStrategy.Unsigned<>(TypedBufferHandler.SHORT_BUFFER_HANDLER));

    private final EncodingStrategy<Short> delegate;
    private static final Set<UnsignedShortEncodingStrategy> UNSIGNED_SHORT_ENCODING_STRATEGY = EnumSet.allOf(UnsignedShortEncodingStrategy.class);

    UnsignedShortEncodingStrategy(EncodingStrategy<Short> delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte getCode() {
        return this.delegate.getCode();
    }

    @Override
    public void encodeValues(Buffer buffer, List<Short> values) {
        this.delegate.encodeValues(buffer, values);
    }

    @Override
    public List<Short> decodeValues(Buffer buffer, int numValues) {
        return this.delegate.decodeValues(buffer, numValues);
    }

    public static UnsignedShortEncodingStrategy getFromCode(int code) {

        for (UnsignedShortEncodingStrategy encodingStrategy : UNSIGNED_SHORT_ENCODING_STRATEGY) {
            if (encodingStrategy.getCode() == (code & 0xFF)) {
                return encodingStrategy;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static class Analyzer implements StrategyAnalyzer<Short> {

        private final EncodingStrategy<Short> bestStrategy;
        private final List<Short> values;

        private Analyzer(EncodingStrategy<Short> bestStrategy, List<Short> values) {
            this.bestStrategy = bestStrategy;
            this.values = values;
        }

        @Override
        public EncodingStrategy<Short> getBestStrategy() {
            return this.bestStrategy;
        }

        @Override
        public List<Short> getValues() {
            return this.values;
        }

        public static class Builder implements StrategyAnalyzerBuilder<Short> {

            private static final int SHORT_BYTE_SIZE = 2;

            private final List<Short> values = new ArrayList<>();
            private short previousValue = 0;

            private int byteSizeValue = 0;
            private int byteSizeRepeatCount = 0;

            private int repeatedValueCount = 0;

            @Override
            public StrategyAnalyzerBuilder<Short> addValue(Short value) {
                if (this.values.isEmpty()) {
                    initializeByteSizes();
                } else {
                    updateByteSizes(value);
                }
                this.previousValue = value;

                this.values.add(value);
                return this;
            }

            @Override
            public StrategyAnalyzer<Short> build() {
                if (this.repeatedValueCount > 0) {
                    this.byteSizeRepeatCount += BytesUtils.computeVar32Size(this.repeatedValueCount);
                }
                EncodingStrategy<Short> bestStrategy;
                int minimumNumBytesUsed = Collections.min(Arrays.asList(
                        this.byteSizeValue,
                        this.byteSizeRepeatCount));
                if (this.byteSizeValue == minimumNumBytesUsed) {
                    bestStrategy = NONE;
                } else {
                    bestStrategy = REPEAT_COUNT;
                }
                List<Short> values = new ArrayList<>(this.values);
                this.values.clear();
                return new Analyzer(bestStrategy, values);
            }

            int getByteSizeValue() {
                return byteSizeValue;
            }

            int getByteSizeRepeatCount() {
                return byteSizeRepeatCount;
            }

            private void initializeByteSizes() {
                this.byteSizeValue = SHORT_BYTE_SIZE;
                this.repeatedValueCount = 1;
                this.byteSizeRepeatCount = SHORT_BYTE_SIZE;
            }

            private void updateByteSizes(int value) {
                this.byteSizeValue += SHORT_BYTE_SIZE;
                if (this.previousValue != value) {
                    this.byteSizeRepeatCount += BytesUtils.computeVar32Size(this.repeatedValueCount);
                    this.byteSizeRepeatCount += 2;
                    this.repeatedValueCount = 1;
                } else {
                    this.repeatedValueCount++;
                }
            }
        }
    }
}
