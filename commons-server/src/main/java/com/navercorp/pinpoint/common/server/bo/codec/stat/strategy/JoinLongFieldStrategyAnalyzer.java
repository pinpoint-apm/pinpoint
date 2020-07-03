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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class JoinLongFieldStrategyAnalyzer implements JoinFieldStrategyAnalyzer<JoinLongFieldBo> {

    private final StrategyAnalyzer<Long> avgValueAnalyzer;
    private final StrategyAnalyzer<Long> minValueAnalyzer;
    private final StrategyAnalyzer<String> minAgentIdAnalyzer;
    private final StrategyAnalyzer<Long> maxValueAnalyzer;
    private final StrategyAnalyzer<String> maxAgentIdAnalyzer;

    private final List<JoinLongFieldBo> values;

    public JoinLongFieldStrategyAnalyzer(StrategyAnalyzer<Long> avgValueAnalyzer,
                                         StrategyAnalyzer<Long> minValueAnalyzer, StrategyAnalyzer<String> minAgentIdAnalyzer,
                                         StrategyAnalyzer<Long> maxValueAnalyzer, StrategyAnalyzer<String> maxAgentIdAnalyzer,
                                         List<JoinLongFieldBo> values) {
        this.avgValueAnalyzer = Objects.requireNonNull(avgValueAnalyzer, "avgValueAnalyzer");
        this.minValueAnalyzer = Objects.requireNonNull(minValueAnalyzer, "minValueAnalyzer");
        this.minAgentIdAnalyzer = Objects.requireNonNull(minAgentIdAnalyzer, "minAgentIdAnalyzer");
        this.maxValueAnalyzer = Objects.requireNonNull(maxValueAnalyzer, "maxValueAnalyzer");
        this.maxAgentIdAnalyzer = Objects.requireNonNull(maxAgentIdAnalyzer, "maxAgentIdAnalyzer");

        this.values = values;
    }

    @Override
    public JoinEncodingStrategy<JoinLongFieldBo> getBestStrategy() {
        final EncodingStrategy<Long> avgValueStrategy = avgValueAnalyzer.getBestStrategy();
        final EncodingStrategy<Long> minValueStrategy = minValueAnalyzer.getBestStrategy();
        final EncodingStrategy<String> minAgentIdStrategy = minAgentIdAnalyzer.getBestStrategy();
        final EncodingStrategy<Long> maxValueStrategy = maxValueAnalyzer.getBestStrategy();
        final EncodingStrategy<String> maxAgentIdStrategy = maxAgentIdAnalyzer.getBestStrategy();

        return new JoinLongFieldEncodingStrategy(avgValueStrategy, minValueStrategy, minAgentIdStrategy, maxValueStrategy, maxAgentIdStrategy);
    }

    @Override
    public List<JoinLongFieldBo> getValues() {
        return values;
    }

    public static class Builder implements JoinFieldStrategyAnalyzerBuilder<JoinLongFieldBo> {

        private final UnsignedLongEncodingStrategy.Analyzer.Builder avgValueAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder minValueAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final StringEncodingStrategy.Analyzer.Builder minAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();
        private final UnsignedLongEncodingStrategy.Analyzer.Builder maxValueAnalyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();
        private final StringEncodingStrategy.Analyzer.Builder maxAgentIdAnalyzerBuilder = new StringEncodingStrategy.Analyzer.Builder();

        private final List<JoinLongFieldBo> values = new ArrayList<JoinLongFieldBo>();

        @Override
        public JoinFieldStrategyAnalyzerBuilder<JoinLongFieldBo> addValue(JoinLongFieldBo value) {
            avgValueAnalyzerBuilder.addValue(value.getAvg());
            minValueAnalyzerBuilder.addValue(value.getMin());
            minAgentIdAnalyzerBuilder.addValue(value.getMinAgentId());
            maxValueAnalyzerBuilder.addValue(value.getMax());
            maxAgentIdAnalyzerBuilder.addValue(value.getMaxAgentId());

            values.add(value);
            return this;
        }

        @Override
        public JoinLongFieldStrategyAnalyzer build() {
            final StrategyAnalyzer<Long> avgValueAnalyzer = avgValueAnalyzerBuilder.build();
            final StrategyAnalyzer<Long> minValueAnalyzer = minValueAnalyzerBuilder.build();
            final StrategyAnalyzer<String> minAgentIdAnalyzer = minAgentIdAnalyzerBuilder.build();
            final StrategyAnalyzer<Long> maxValueAnalyzer = maxValueAnalyzerBuilder.build();
            final StrategyAnalyzer<String> maxAgentIdAnalyzer = maxAgentIdAnalyzerBuilder.build();

            return new JoinLongFieldStrategyAnalyzer(avgValueAnalyzer, minValueAnalyzer, minAgentIdAnalyzer, maxValueAnalyzer, maxAgentIdAnalyzer, values);
        }
    }

}
