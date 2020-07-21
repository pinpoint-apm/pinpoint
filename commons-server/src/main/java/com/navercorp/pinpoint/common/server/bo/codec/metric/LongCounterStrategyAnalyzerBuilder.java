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
import com.navercorp.pinpoint.common.server.bo.codec.stat.strategy.UnsignedLongEncodingStrategy;
import com.navercorp.pinpoint.common.server.bo.metric.CustomMetricValue;
import com.navercorp.pinpoint.common.server.bo.metric.EachCustomMetricBo;
import com.navercorp.pinpoint.common.server.bo.metric.LongCounterMetricValue;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class LongCounterStrategyAnalyzerBuilder implements CustomMetricStrategyAnalyzerBuilder<Long> {

    public static final long UNCOLLECTED_VALUE = -1L;

    private final UnsignedLongEncodingStrategy.Analyzer.Builder analyzerBuilder = new UnsignedLongEncodingStrategy.Analyzer.Builder();

    private final String metricName;
    private final long uncollectedValue;

    public LongCounterStrategyAnalyzerBuilder(String metricName) {
        this(metricName, UNCOLLECTED_VALUE);
    }

    public LongCounterStrategyAnalyzerBuilder(String metricName, long uncollectedValue) {
        this.metricName = Objects.requireNonNull(metricName, "metricName");
        this.uncollectedValue = uncollectedValue;
    }

    @Override
    public CustomMetricStrategyAnalyzerBuilder<Long> addValue(EachCustomMetricBo eachCustomMetricBo) {
        final CustomMetricValue customMetricValue = eachCustomMetricBo.get(metricName);

        if (!(customMetricValue instanceof LongCounterMetricValue)) {
            throw new IllegalArgumentException(metricName + " must be LongCountMetricBo clazz");
        }

        LongCounterMetricValue longCounterMetricValue = (LongCounterMetricValue) customMetricValue;
        Long value = longCounterMetricValue.getValue();

        if (value != null) {
            analyzerBuilder.addValue(value);
        } else {
            analyzerBuilder.addValue(uncollectedValue);
        }

        return this;
    }

    @Override
    public StrategyAnalyzer<Long> build() {
        return analyzerBuilder.build();
    }

}
