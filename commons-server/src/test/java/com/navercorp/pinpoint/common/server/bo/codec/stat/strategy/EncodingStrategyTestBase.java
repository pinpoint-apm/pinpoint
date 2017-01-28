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

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public abstract class EncodingStrategyTestBase<T extends Number> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentStatDataPointCodec codec = new AgentStatDataPointCodec();

    protected static final int NUM_TEST_RUNS = 20;

    protected abstract StrategyAnalyzer.StrategyAnalyzerBuilder<T> getStrategyAnalyzerBuilder();

    protected abstract List<EncodingStrategy<T>> getEncodingStrategies();

    protected abstract void checkBuilder(StrategyAnalyzer.StrategyAnalyzerBuilder<T> analyzerBuilder, Map<EncodingStrategy<T>, Integer> bufferSizes);

    protected void testFor(List<T> testValues) {
        StrategyAnalyzer.StrategyAnalyzerBuilder<T> builder = this.getStrategyAnalyzerBuilder();
        for (T testValue : testValues) {
            builder.addValue(testValue);
        }

        final StrategyAnalyzer<T> analyzer = builder.build();
        logger.debug("Values : {}", analyzer.getValues());

        Map<EncodingStrategy<T>, Integer> bufferSizes = this.getBufferSizes(testValues);
        checkBuilder(builder, bufferSizes);
        checkStrategy(analyzer, getBestEncodingStrategies(bufferSizes));
    }

    private void checkStrategy(StrategyAnalyzer<T> analyzer, Set<? extends EncodingStrategy<T>> bestStrategies) {
        EncodingStrategy<T> chosenStrategy = analyzer.getBestStrategy();
        List<T> values = analyzer.getValues();
        logger.debug("Chosen : {}", analyzer.getBestStrategy());
        Assert.assertTrue(createTestFailMessage(values, bestStrategies, chosenStrategy), bestStrategies.contains(chosenStrategy));
    }

    private <S extends EncodingStrategy<T>> String createTestFailMessage(List<T> values, Set<S> bestStrategies, EncodingStrategy<T> chosenStrategy) {
        StringBuilder sb = new StringBuilder("Wrong strategy chosen - ");
        sb.append("expected one of ").append(bestStrategies);
        sb.append(", but got ").append(chosenStrategy);
        sb.append(". Value : ").append(values);
        return sb.toString();
    }

    private Set<EncodingStrategy<T>> getBestEncodingStrategies(Map<EncodingStrategy<T>, Integer> bufferSizes) {
        int minimumBufferSize = Integer.MAX_VALUE;
        Set<EncodingStrategy<T>> bestStrategies = new HashSet<EncodingStrategy<T>>();
        for (Map.Entry<EncodingStrategy<T>, Integer> entry : bufferSizes.entrySet()) {
            EncodingStrategy<T> strategy = entry.getKey();
            int bufferSize = entry.getValue();
            if (bufferSize < minimumBufferSize) {
                bestStrategies.clear();
                bestStrategies.add(strategy);
                minimumBufferSize = bufferSize;
            } else if (bufferSize == minimumBufferSize) {
                bestStrategies.add(strategy);
            }
        }
        return bestStrategies;
    }

    private Map<EncodingStrategy<T>, Integer> getBufferSizes(List<T> values) {
        Map<EncodingStrategy<T>, Integer> bufferSizes = new HashMap<EncodingStrategy<T>, Integer>();
        for (EncodingStrategy<T> strategy : getEncodingStrategies()) {
            Buffer encodedBuffer = new AutomaticBuffer();
            codec.encodeValues(encodedBuffer, strategy, values);
            int encodedBufferSize = encodedBuffer.getBuffer().length;
            bufferSizes.put(strategy, encodedBufferSize);
        }
        logger.debug("Strategies : {}", bufferSizes);
        return bufferSizes;
    }
}
