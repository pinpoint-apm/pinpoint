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
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPointSummary;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentStatPointFactory {

    private final Point.UncollectedPointCreator<AgentStatPoint<Integer>> uncollectedIntValuePointCreator;
    private final Point.UncollectedPointCreator<AgentStatPoint<Long>> uncollectedLongValuePointCreator;
    private final Point.UncollectedPointCreator<AgentStatPoint<Double>> uncollectedDoubleValuePointCreator;

    private final int defaultValueDecimal;

    public AgentStatPointFactory(int uncollectedIntValue, long uncollectedLongValue, double uncollectedDoubleValue) {
        this(uncollectedIntValue, uncollectedLongValue, uncollectedDoubleValue, 0);
    }

    public AgentStatPointFactory(int uncollectedIntValue, long uncollectedLongValue, double uncollectedDoubleValue, int defaultValueDecimal) {
        this.uncollectedIntValuePointCreator = UncollectedPointCreatorFactory.createIntPointCreator(uncollectedIntValue);
        this.uncollectedLongValuePointCreator = UncollectedPointCreatorFactory.createLongPointCreator(uncollectedLongValue);
        this.uncollectedDoubleValuePointCreator = UncollectedPointCreatorFactory.createDoublePointCreator(uncollectedDoubleValue);

        this.defaultValueDecimal = defaultValueDecimal;
    }

    public AgentStatPoint<Integer> createIntPoint(long timestamp, List<Integer> values) {
        return createIntPoint(timestamp, values, defaultValueDecimal);
    }

    public AgentStatPoint<Integer> createIntPoint(long timestamp, List<Integer> values, int numDecimals) {
        if (CollectionUtils.isEmpty(values)) {
            return uncollectedIntValuePointCreator.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.intSummary(timestamp, values, numDecimals);
    }

    public AgentStatPoint<Long> createLongPoint(long timestamp, List<Long> values) {
        return createLongPoint(timestamp, values, defaultValueDecimal);
    }

    public AgentStatPoint<Long> createLongPoint(long timestamp, List<Long> values, int numDecimals) {
        if (CollectionUtils.isEmpty(values)) {
            return uncollectedLongValuePointCreator.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.longSummary(timestamp, values, numDecimals);
    }

    public AgentStatPoint<Double> createDoublePoint(long timestamp, List<Double> values) {
        return createDoublePoint(timestamp, values, defaultValueDecimal);
    }

    public AgentStatPoint<Double> createDoublePoint(long timestamp, List<Double> values, int numDecimals) {
        if (CollectionUtils.isEmpty(values)) {
            return uncollectedDoubleValuePointCreator.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.doubleSummary(timestamp, values, numDecimals);
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
