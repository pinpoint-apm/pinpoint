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
public class UnsignedIntegerEncodingStrategyTest extends EncodingStrategyTestBase<Integer> {

    @Override
    protected StrategyAnalyzer.StrategyAnalyzerBuilder<Integer> getStrategyAnalyzerBuilder() {
        return new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
    }

    @Override
    protected List<EncodingStrategy<Integer>> getEncodingStrategies() {
        return Arrays.<EncodingStrategy<Integer>>asList(UnsignedIntegerEncodingStrategy.values());
    }

    @Override
    protected void checkBuilder(StrategyAnalyzer.StrategyAnalyzerBuilder<Integer> analyzerBuilder, Map<EncodingStrategy<Integer>, Integer> bufferSizes) {
        UnsignedIntegerEncodingStrategy.Analyzer.Builder builder = (UnsignedIntegerEncodingStrategy.Analyzer.Builder) analyzerBuilder;
        int actualValueEncodedSize = bufferSizes.get(UnsignedIntegerEncodingStrategy.NONE);
        int actualRepeatCountEncodedSize = bufferSizes.get(UnsignedIntegerEncodingStrategy.REPEAT_COUNT);
        int actualDeltaEncodedSize = bufferSizes.get(UnsignedIntegerEncodingStrategy.DELTA);
        int actualDeltaOfDeltaEncodedSize = bufferSizes.get(UnsignedIntegerEncodingStrategy.DELTA_OF_DELTA);
        Assert.assertEquals(actualValueEncodedSize, builder.getByteSizeValue());
        Assert.assertEquals(actualRepeatCountEncodedSize, builder.getByteSizeRepeatCount());
        Assert.assertEquals(actualDeltaEncodedSize, builder.getByteSizeDelta());
        Assert.assertEquals(actualDeltaOfDeltaEncodedSize, builder.getByteSizeDeltaOfDelta());
    }

    @Test
    public void test_small_values() {
        int minValue = 10;
        int maxValue = 100;
        testValues(minValue, maxValue);
    }

    @Test
    public void test_medium_values() {
        int minValue = 1000;
        int maxValue = 1000000;
        testValues(minValue, maxValue);
    }
    @Test
    public void test_large_values() {
        int minValue = 1000000;
        int maxValue = 1000000000;
        testValues(minValue, maxValue);
    }

    private void testValues(int minValue, int maxValue) {
        for (int i = 0; i < NUM_TEST_RUNS; ++i) {
            List<Integer> constantValues = TestAgentStatDataPointFactory.INTEGER.createConstantValues(minValue, maxValue);
            testFor(constantValues);
            List<Integer> randomValues = TestAgentStatDataPointFactory.INTEGER.createRandomValues(minValue, maxValue);
            testFor(randomValues);
            List<Integer> increasingValues1 = TestAgentStatDataPointFactory.INTEGER.createIncreasingValues(minValue, maxValue, 0, minValue / 10);
            testFor(increasingValues1);
            List<Integer> increasingValues2 = TestAgentStatDataPointFactory.INTEGER.createIncreasingValues(minValue, maxValue, minValue / 10, maxValue / 10);
            testFor(increasingValues2);
            List<Integer> decreasingValues1 = TestAgentStatDataPointFactory.INTEGER.createDecreasingValues(minValue, maxValue, 0, minValue / 10);
            testFor(decreasingValues1);
            List<Integer> decreasingValues2 = TestAgentStatDataPointFactory.INTEGER.createDecreasingValues(minValue, maxValue, minValue / 10, maxValue / 10);
            testFor(decreasingValues2);
            List<Integer> fluctuatingValues1 = TestAgentStatDataPointFactory.INTEGER.createFluctuatingValues(minValue, maxValue, 0, minValue / 10);
            testFor(fluctuatingValues1);
            List<Integer> fluctuatingValues2 = TestAgentStatDataPointFactory.INTEGER.createFluctuatingValues(minValue, maxValue, minValue / 10, maxValue / 10);
            testFor(fluctuatingValues2);
        }
    }
}
