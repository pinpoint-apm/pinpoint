/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.StringTypedBufferHandler;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.impl.StringAlwaysSameValueEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.impl.StringRepeatCountEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.impl.StringValueEncodingStrategy;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public enum StringEncodingStrategy implements EncodingStrategy<String> {
    NONE(new StringValueEncodingStrategy(StringTypedBufferHandler.VARIABLE_HANDLER)),
    REPEAT_COUNT(new StringRepeatCountEncodingStrategy(StringTypedBufferHandler.VARIABLE_HANDLER)),
    ALWAYS_SAME_VALUE(new StringAlwaysSameValueEncodingStrategy(StringTypedBufferHandler.VARIABLE_HANDLER));

    private final EncodingStrategy<String> delegate;

    private static final Set<StringEncodingStrategy> STRING_ENCODING_STRATEGY = EnumSet.allOf(StringEncodingStrategy.class);

    StringEncodingStrategy(EncodingStrategy<String> delegate) {
        this.delegate = delegate;
    }

    @Override
    public byte getCode() {
        return this.delegate.getCode();
    }

    @Override
    public void encodeValues(Buffer buffer, List<String> values) {
        this.delegate.encodeValues(buffer, values);
    }

    @Override
    public List<String> decodeValues(Buffer buffer, int numValues) {
        return this.delegate.decodeValues(buffer, numValues);
    }

    public static StringEncodingStrategy getFromCode(int code) {
        for (StringEncodingStrategy encodingStrategy : STRING_ENCODING_STRATEGY) {
            if (encodingStrategy.getCode() == (code & 0xFF)) {
                return encodingStrategy;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static class Analyzer implements StrategyAnalyzer<String> {

        private final EncodingStrategy<String> bestStrategy;
        private final List<String> values;

        private Analyzer(EncodingStrategy<String> bestStrategy, List<String> values) {
            this.bestStrategy = bestStrategy;
            this.values = values;
        }

        @Override
        public EncodingStrategy<String> getBestStrategy() {
            return this.bestStrategy;
        }

        @Override
        public List<String> getValues() {
            return this.values;
        }

        public static class Builder implements StrategyAnalyzerBuilder<String> {

            private static final int MAX_BYTES_PER_CHAR_UTF8 = (int) Charsets.UTF_8.newEncoder().maxBytesPerChar();

            private final List<String> values = new ArrayList<String>();

            private String previousValue = null;

            private int byteSizeValue = 0;

            private int repeatedValueCount = 0;
            private int byteSizeRepeatCount = 0;


            @Override
            public StrategyAnalyzerBuilder<String> addValue(String value) {
                if (this.values.isEmpty()) {
                    initializeByteSizes(value);
                } else {
                    updateByteSizes(value);
                }
                this.previousValue = value;

                this.values.add(value);
                return this;
            }

            @Override
            public StrategyAnalyzer<String> build() {
                if (this.repeatedValueCount > 0) {
                    this.byteSizeRepeatCount += BytesUtils.computeVar32Size(this.repeatedValueCount);
                }
                EncodingStrategy<String> bestStrategy;
                if (repeatedValueCount != 0 && repeatedValueCount == values.size()) {
                    bestStrategy = ALWAYS_SAME_VALUE;
                } else {
                    int minimumNumBytesUsed = Collections.min(Arrays.asList(
                            this.byteSizeValue,
                            this.byteSizeRepeatCount));
                    if (this.byteSizeValue == minimumNumBytesUsed) {
                        bestStrategy = NONE;
                    } else {
                        bestStrategy = REPEAT_COUNT;
                    }
                }

                List<String> values = new ArrayList<String>(this.values);
                this.values.clear();

                return new Analyzer(bestStrategy, values);
            }

            private void initializeByteSizes(String value) {
                int maxBytesUsedByValue = getMaxBytes(value);
                this.byteSizeValue = maxBytesUsedByValue;
                this.repeatedValueCount = 1;
                this.byteSizeRepeatCount = maxBytesUsedByValue;
            }

            private void updateByteSizes(String value) {
                int maxBytesUsedByValue = getMaxBytes(value);
                this.byteSizeValue += maxBytesUsedByValue;

                // for null
                if (this.previousValue == null) {
                    if (value == null) {
                        this.repeatedValueCount++;
                    } else {
                        this.byteSizeRepeatCount += expectedBytesVLength(this.repeatedValueCount);
                        this.byteSizeRepeatCount += maxBytesUsedByValue;
                        this.repeatedValueCount = 1;
                    }
                } else if (this.previousValue.equals(value)) {
                    this.repeatedValueCount++;
                } else {
                    this.byteSizeRepeatCount += expectedBytesVLength(this.repeatedValueCount);
                    this.byteSizeRepeatCount += maxBytesUsedByValue;
                    this.repeatedValueCount = 1;
                }
            }

            private int getMaxBytes(String value) {
                if (value == null) {
                    return 0;
                }
                return value.length() * MAX_BYTES_PER_CHAR_UTF8;
            }

            private int expectedBytesVLength(int value) {
                if (value < 0) {
                    return BytesUtils.computeVar64Size(value);
                } else {
                    return BytesUtils.computeVar32Size(value);
                }
            }

        }

    }
}
