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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPointCreatorFactory;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentStatPointFactory {

    private final Point.UncollectedPointCreator<AgentStatPoint<Integer>> uncollectedIntValuePointCreator;
    private final DownSampler<Integer> intDownSampler;

    private final Point.UncollectedPointCreator<AgentStatPoint<Long>> uncollectedLongValuePointCreator;
    private final DownSampler<Long> longDownSampler;

    private final Point.UncollectedPointCreator<AgentStatPoint<Double>> uncollectedDoubleValuePointCreator;
    private final DownSampler<Double> doubleDownSampler;

    private final int defaultValueDecimal;

    public AgentStatPointFactory(int uncollectedIntValue, long uncollectedLongValue, double uncollectedDoubleValue) {
        this(uncollectedIntValue, uncollectedLongValue, uncollectedDoubleValue, 0);
    }

    public AgentStatPointFactory(int uncollectedIntValue, long uncollectedLongValue, double uncollectedDoubleValue, int defaultValueDecimal) {
        this.uncollectedIntValuePointCreator = UncollectedPointCreatorFactory.createIntPointCreator(uncollectedIntValue);
        this.intDownSampler = DownSamplers.getIntegerDownSampler(uncollectedIntValue);

        this.uncollectedLongValuePointCreator = UncollectedPointCreatorFactory.createLongPointCreator(uncollectedLongValue);
        this.longDownSampler = DownSamplers.getLongDownSampler(uncollectedLongValue);

        this.uncollectedDoubleValuePointCreator = UncollectedPointCreatorFactory.createDoublePointCreator(uncollectedDoubleValue);
        this.doubleDownSampler = DownSamplers.getDoubleDownSampler(uncollectedDoubleValue);

        this.defaultValueDecimal = defaultValueDecimal;
    }

    public AgentStatPoint<Integer> createIntPoint(long timestamp, List<Integer> values) {
        return createIntPoint(timestamp, values, defaultValueDecimal);
    }

    public AgentStatPoint<Integer> createIntPoint(long timestamp, List<Integer> values, int numDecimals) {
        if (values.isEmpty()) {
            return uncollectedIntValuePointCreator.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                intDownSampler.sampleMin(values),
                intDownSampler.sampleMax(values),
                intDownSampler.sampleAvg(values, numDecimals),
                intDownSampler.sampleSum(values));
    }

    public AgentStatPoint<Long> createLongPoint(long timestamp, List<Long> values) {
        return createLongPoint(timestamp, values, defaultValueDecimal);
    }

    public AgentStatPoint<Long> createLongPoint(long timestamp, List<Long> values, int numDecimals) {
        if (values.isEmpty()) {
            return uncollectedLongValuePointCreator.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                longDownSampler.sampleMin(values),
                longDownSampler.sampleMax(values),
                longDownSampler.sampleAvg(values, numDecimals),
                longDownSampler.sampleSum(values));
    }

    public AgentStatPoint<Double> createDoublePoint(long timestamp, List<Double> values) {
        return createDoublePoint(timestamp, values, defaultValueDecimal);
    }

    public AgentStatPoint<Double> createDoublePoint(long timestamp, List<Double> values, int numDecimals) {
        if (values.isEmpty()) {
            return uncollectedDoubleValuePointCreator.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                doubleDownSampler.sampleMin(values),
                doubleDownSampler.sampleMax(values),
                doubleDownSampler.sampleAvg(values, numDecimals),
                doubleDownSampler.sampleSum(values));
    }

    public Point.UncollectedPointCreator<AgentStatPoint<Integer>> getUncollectedIntValuePointCreator() {
        return uncollectedIntValuePointCreator;
    }

    public Point.UncollectedPointCreator<AgentStatPoint<Long>> getUncollectedLongValuePointCreator() {
        return uncollectedLongValuePointCreator;
    }

    public Point.UncollectedPointCreator<AgentStatPoint<Double>> getUncollectedDoubleValuePointCreator() {
        return uncollectedDoubleValuePointCreator;
    }
}
