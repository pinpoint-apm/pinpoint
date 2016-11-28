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
public class UnsignedShortEncodingStrategyTest extends EncodingStrategyTestBase<Short> {

    @Override
    protected StrategyAnalyzer.StrategyAnalyzerBuilder<Short> getStrategyAnalyzerBuilder() {
        return new UnsignedShortEncodingStrategy.Analyzer.Builder();
    }

    @Override
    protected List<EncodingStrategy<Short>> getEncodingStrategies() {
        return Arrays.<EncodingStrategy<Short>>asList(UnsignedShortEncodingStrategy.values());
    }

    @Override
    protected void checkBuilder(StrategyAnalyzer.StrategyAnalyzerBuilder<Short> analyzerBuilder, Map<EncodingStrategy<Short>, Integer> bufferSizes) {
        UnsignedShortEncodingStrategy.Analyzer.Builder builder = (UnsignedShortEncodingStrategy.Analyzer.Builder) analyzerBuilder;
        int actualValueEncodedSize = bufferSizes.get(UnsignedShortEncodingStrategy.NONE);
        int actualRepeatCountEncodedSize = bufferSizes.get(UnsignedShortEncodingStrategy.REPEAT_COUNT);
        Assert.assertEquals("none", actualValueEncodedSize, builder.getByteSizeValue());
        Assert.assertEquals("repeatCount", actualRepeatCountEncodedSize, builder.getByteSizeRepeatCount());
    }

    @Test
    public void test_small_values() {
        short minValue = 10;
        short maxValue = 100;
        testValues(minValue, maxValue);
    }

    @Test
    public void test_medium_values() {
        short minValue = 100;
        short maxValue = 1000;
        testValues(minValue, maxValue);
    }

    @Test
    public void test_large_values() {
        short minValue = 1000;
        short maxValue = 10000;
        testValues(minValue, maxValue);
    }

    private void testValues(short minValue, short maxValue) {
        for (int i = 0; i < NUM_TEST_RUNS; ++i) {
            List<Short> constantValues = TestAgentStatDataPointFactory.SHORT.createConstantValues(minValue, maxValue);
            testFor(constantValues);
            List<Short> randomValues = TestAgentStatDataPointFactory.SHORT.createRandomValues(minValue, maxValue);
            testFor(randomValues);
            List<Short> increasingValues1 = TestAgentStatDataPointFactory.SHORT.createIncreasingValues(minValue, maxValue, (short) 0, (short) (minValue / 10));
            testFor(increasingValues1);
            List<Short> increasingValues2 = TestAgentStatDataPointFactory.SHORT.createIncreasingValues(minValue, maxValue, (short) (minValue / 10), (short) (maxValue / 10));
            testFor(increasingValues2);
            List<Short> decreasingValues1 = TestAgentStatDataPointFactory.SHORT.createDecreasingValues(minValue, maxValue, (short) 0, (short) (minValue / 10));
            testFor(decreasingValues1);
            List<Short> decreasingValues2 = TestAgentStatDataPointFactory.SHORT.createDecreasingValues(minValue, maxValue, (short) (minValue / 10), (short) (maxValue / 10));
            testFor(decreasingValues2);
            List<Short> fluctuatingValues1 = TestAgentStatDataPointFactory.SHORT.createFluctuatingValues(minValue, maxValue, (short) 0, (short) (minValue / 10));
            testFor(fluctuatingValues1);
            List<Short> fluctuatingValues2 = TestAgentStatDataPointFactory.SHORT.createFluctuatingValues(minValue, maxValue, (short) (minValue / 10), (short) (maxValue / 10));
            testFor(fluctuatingValues2);
        }
    }
}
