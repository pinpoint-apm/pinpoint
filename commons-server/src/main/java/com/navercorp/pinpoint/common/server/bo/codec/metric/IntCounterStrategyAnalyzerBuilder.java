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

package com.navercorp.pinpoint.common.server.bo.codec.metric;

import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.StrategyAnalyzer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedIntegerEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricValue;
import com.navercorp.pinpoint.common.server.bo.metric.EachCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.IntCounterMetricValue;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class IntCounterStrategyAnalyzerBuilder implements CustomMetricStrategyAnalyzerBuilder<Integer> {

    public static final int UNCOLLECTED_VALUE = -1;

    private final UnsignedIntegerEncodingStrategy.Analyzer.Builder analyzerBuilder = new UnsignedIntegerEncodingStrategy.Analyzer.Builder();

    private final String metricName;
    private final int uncollectedValue;

    public IntCounterStrategyAnalyzerBuilder(String metricName) {
        this(metricName, UNCOLLECTED_VALUE);
    }

    public IntCounterStrategyAnalyzerBuilder(String metricName, int uncollectedValue) {
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.uncollectedValue = uncollectedValue;
    }

    @Override
    public CustomMetricStrategyAnalyzerBuilder<Integer> addValue(EachCustomMetricBo eachCustomMetricBo) {
        final CustomMetricValue customMetricValue = eachCustomMetricBo.get(metricName);

        if (!(customMetricValue instanceof IntCounterMetricValue)) {
            throw new IllegalArgumentException(metricName + " must be IntCounterMetricValue clazz");
        }

        IntCounterMetricValue intCounterMetricValue = (IntCounterMetricValue) customMetricValue;
        Integer value = intCounterMetricValue.getValue();

        if (value != null) {
            analyzerBuilder.addValue(value);
        } else {
            analyzerBuilder.addValue(uncollectedValue);
        }
        return this;
    }

    @Override
    public StrategyAnalyzer<Integer> build() {
        return analyzerBuilder.build();
    }

}
