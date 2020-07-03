/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.codec.stat.strategy;

import com.navercorp.pinpoint.common.server.bo.codec.strategy.EncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class JoinIntFieldStrategyAnalyzer implements JoinFieldStrategyAnalyzer<JoinIntFieldBo> {

    private final StrategyAnalyzer<Integer> avgValueAnalyzer;
    private final StrategyAnalyzer<Integer> minValueAnalyzer;
    private final StrategyAnalyzer<String> minAgentIdAnalyzer;
    private final StrategyAnalyzer<Integer> maxValueAnalyzer;
    private final StrategyAnalyzer<String> maxAgentIdAnalyzer;

    private final List<JoinIntFieldBo> values;

    public JoinIntFieldStrategyAnalyzer(StrategyAnalyzer<Integer> avgValueAnalyzer,
                                        StrategyAnalyzer<Integer> minValueAnalyzer, StrategyAnalyzer<String> minAgentIdAnalyzer,
                                        StrategyAnalyzer<Integer> maxValueAnalyzer, StrategyAnalyzer<String> maxAgentIdAnalyzer,
                                        List<JoinIntFieldBo> values) {
        this.avgValueAnalyzer = Objects.requireNonNull(avgValueAnalyzer, "avgValueAnalyzer");
        this.minValueAnalyzer = Objects.requireNonNull(minValueAnalyzer, "minValueAnalyzer");
        this.minAgentIdAnalyzer = Objects.requireNonNull(minAgentIdAnalyzer, "minAgentIdAnalyzer");
        this.maxValueAnalyzer = Objects.requireNonNull(maxValueAnalyzer, "maxValueAnalyzer");
        this.maxAgentIdAnalyzer = Objects.requireNonNull(maxAgentIdAnalyzer, "maxAgentIdAnalyzer");

        this.values = values;
    }

    @Override
    public JoinEncodingStrategy<JoinIntFieldBo> getBestStrategy() {
        final EncodingStrategy<Integer> avgValueStrategy = avgValueAnalyzer.getBestStrategy();
        final EncodingStrategy<Integer> minValueStrategy = minValueAnalyzer.getBestStrategy();
        final EncodingStrategy<String> minAgentIdStrategy = minAgentIdAnalyzer.getBestStrategy();
        final EncodingStrategy<Integer> maxValueStrategy = maxValueAnalyzer.getBestStrategy();
        final EncodingStrategy<String> maxAgentIdStrategy = maxAgentIdAnalyzer.getBestStrategy();

        return new JoinIntFieldEncodingStrategy(avgValueStrategy, minValueStrategy, minAgentIdStrategy, maxValueStrategy, maxAgentIdStrategy);
    }

    @Override
    public List<JoinIntFieldBo> getValues() {
        return values;
    }

    public static class Builder implements JoinFieldStrategyAnalyzer.JoinFieldStrategyAnalyzerBuilder<JoinIntFieldBo> {

        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder avgValueAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder minValueAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final StringEncodingStrategy.Analyzer.Builder minAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        private final UnsignedIntegerEncodingStrategy.Analyzer.Builder maxValueAnalyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();
        private final StringEncodingStrategy.Analyzer.Builder maxAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        private final List<JoinIntFieldBo> values = new ArrayList<JoinIntFieldBo>();


        @Override
        public JoinFieldStrategyAnalyzerBuilder<JoinIntFieldBo> addValue(JoinIntFieldBo value) {
            avgValueAnalyzerBuilder.addValue(value.getAvg());
            minValueAnalyzerBuilder.addValue(value.getMin());
            minAgentIdAnalyzerBuilder.addValue(value.getMinAgentId());
            maxValueAnalyzerBuilder.addValue(value.getMax());
            maxAgentIdAnalyzerBuilder.addValue(value.getMaxAgentId());

            values.add(value);
            return this;
        }

        @Override
        public JoinIntFieldStrategyAnalyzer build() {
            final StrategyAnalyzer<Integer> avgValueAnalyzer = avgValueAnalyzerBuilder.build();
            final StrategyAnalyzer<Integer> minValueAnalyzer = minValueAnalyzerBuilder.build();
            final StrategyAnalyzer<String> minAgentIdAnalyzer = minAgentIdAnalyzerBuilder.build();
            final StrategyAnalyzer<Integer> maxValueAnalyzer = maxValueAnalyzerBuilder.build();
            final StrategyAnalyzer<String> maxAgentIdAnalyzer = maxAgentIdAnalyzerBuilder.build();

            return new JoinIntFieldStrategyAnalyzer(avgValueAnalyzer, minValueAnalyzer, minAgentIdAnalyzer, maxValueAnalyzer, maxAgentIdAnalyzer, values);
        }
    }

}
