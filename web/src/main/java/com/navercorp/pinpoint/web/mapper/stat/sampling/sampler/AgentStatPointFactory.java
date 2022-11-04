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
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPointSummary;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DoubleAgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.IntAgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.LongAgentStatPoint;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentStatPointFactory {

    private final Point.UncollectedPointCreator<IntAgentStatPoint> uncollectedIntValuePointCreator;
    private final Point.UncollectedPointCreator<LongAgentStatPoint> uncollectedLongValuePointCreator;
    private final Point.UncollectedPointCreator<DoubleAgentStatPoint> uncollectedDoubleValuePointCreator;

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

    public IntAgentStatPoint createIntPoint(long timestamp, List<Integer> values) {
        return createIntPoint(timestamp, values, defaultValueDecimal);
    }

    public IntAgentStatPoint createIntPoint(long timestamp, List<Integer> values, int numDecimals) {
        if (CollectionUtils.isEmpty(values)) {
            return uncollectedIntValuePointCreator.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.intSummary(timestamp, values, numDecimals);
    }

    public LongAgentStatPoint createLongPoint(long timestamp, List<Long> values) {
        return createLongPoint(timestamp, values, defaultValueDecimal);
    }

    public LongAgentStatPoint createLongPoint(long timestamp, List<Long> values, int numDecimals) {
        if (CollectionUtils.isEmpty(values)) {
            return uncollectedLongValuePointCreator.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.longSummary(timestamp, values, numDecimals);
    }

    public DoubleAgentStatPoint createDoublePoint(long timestamp, List<Double> values) {
        return createDoublePoint(timestamp, values, defaultValueDecimal);
    }

    public DoubleAgentStatPoint createDoublePoint(long timestamp, List<Double> values, int numDecimals) {
        if (CollectionUtils.isEmpty(values)) {
            return uncollectedDoubleValuePointCreator.createUnCollectedPoint(timestamp);
        }
        return AgentStatPointSummary.doubleSummary(timestamp, values, numDecimals);
    }

    public Point.UncollectedPointCreator<IntAgentStatPoint> getUncollectedIntValuePointCreator() {
        return uncollectedIntValuePointCreator;
    }

    public Point.UncollectedPointCreator<LongAgentStatPoint> getUncollectedLongValuePointCreator() {
        return uncollectedLongValuePointCreator;
    }

    public Point.UncollectedPointCreator<DoubleAgentStatPoint> getUncollectedDoubleValuePointCreator() {
        return uncollectedDoubleValuePointCreator;
    }
}
