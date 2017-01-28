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

import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatDataPointFactory;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class UnsignedLongEncodingStrategyTest extends EncodingStrategyTestBase<Long> {

    @Override
    protected StrategyAnalyzer.StrategyAnalyzerBuilder<Long> getStrategyAnalyzerBuilder() {
        return new UnsignedLongEncodingStrategy.Analyzer.Builder();
    }

    @Override
    protected List<EncodingStrategy<Long>> getEncodingStrategies() {
        return Arrays.<EncodingStrategy<Long>>asList(UnsignedLongEncodingStrategy.values());
    }

    @Override
    protected void checkBuilder(StrategyAnalyzer.StrategyAnalyzerBuilder<Long> analyzerBuilder, Map<EncodingStrategy<Long>, Integer> bufferSizes) {
        UnsignedLongEncodingStrategy.Analyzer.Builder builder = (UnsignedLongEncodingStrategy.Analyzer.Builder) analyzerBuilder;
        int actualValueEncodedSize = bufferSizes.get(UnsignedLongEncodingStrategy.NONE);
        int actualRepeatCountEncodedSize = bufferSizes.get(UnsignedLongEncodingStrategy.REPEAT_COUNT);
        int actualDeltaEncodedSize = bufferSizes.get(UnsignedLongEncodingStrategy.DELTA);
        int actualDeltaOfDeltaEncodedSize = bufferSizes.get(UnsignedLongEncodingStrategy.DELTA_OF_DELTA);
        Assert.assertEquals(actualValueEncodedSize, builder.getByteSizeValue());
        Assert.assertEquals(actualRepeatCountEncodedSize, builder.getByteSizeRepeatCount());
        Assert.assertEquals(actualDeltaEncodedSize, builder.getByteSizeDelta());
        Assert.assertEquals(actualDeltaOfDeltaEncodedSize, builder.getByteSizeDeltaOfDelta());
    }

    @Test
    public void test_small_values() {
        long minValue = 10;
        long maxValue = 100;
        testValues(minValue, maxValue);
    }

    @Test
    public void test_medium_values() {
        long minValue = 1000;
        long maxValue = 1000000;
        testValues(minValue, maxValue);
    }

    @Test
    public void test_large_values() {
        long minValue = 1000000;
        long maxValue = 1000000000;
        testValues(minValue, maxValue);
    }
    @Test
    public void test_huge_values() {
        long minValue = 1000000000;
        long maxValue = 100000000000000000L;
        testValues(minValue, maxValue);
    }

    private void testValues(long minValue, long maxValue) {
        for (int i = 0; i < NUM_TEST_RUNS; ++i) {
            List<Long> constantValues = TestAgentStatDataPointFactory.LONG.createConstantValues(minValue, maxValue);
            testFor(constantValues);
            List<Long> randomValues = TestAgentStatDataPointFactory.LONG.createRandomValues(minValue, maxValue);
            testFor(randomValues);
            List<Long> increasingValues1 = TestAgentStatDataPointFactory.LONG.createIncreasingValues(minValue, maxValue, 0L, minValue / 10);
            testFor(increasingValues1);
            List<Long> increasingValues2 = TestAgentStatDataPointFactory.LONG.createIncreasingValues(minValue, maxValue, minValue / 10, maxValue / 10);
            testFor(increasingValues2);
            List<Long> decreasingValues1 = TestAgentStatDataPointFactory.LONG.createDecreasingValues(minValue, maxValue, 0L, minValue / 10);
            testFor(decreasingValues1);
            List<Long> decreasingValues2 = TestAgentStatDataPointFactory.LONG.createDecreasingValues(minValue, maxValue, minValue / 10, maxValue / 10);
            testFor(decreasingValues2);
            List<Long> fluctuatingValues1 = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(minValue, maxValue, 0L, minValue / 10);
            testFor(fluctuatingValues1);
            List<Long> fluctuatingValues2 = TestAgentStatDataPointFactory.LONG.createFluctuatingValues(minValue, maxValue, minValue / 10, maxValue / 10);
            testFor(fluctuatingValues2);
        }
    }
}
