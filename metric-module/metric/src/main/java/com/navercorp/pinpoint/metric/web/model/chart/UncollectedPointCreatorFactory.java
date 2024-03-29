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

package com.navercorp.pinpoint.metric.web.model.chart;


import com.navercorp.pinpoint.metric.common.model.chart.Point;

/**
 * @author Taejin Koo
 */
public class UncollectedPointCreatorFactory {

    public static Point.UncollectedPointCreator<AgentStatPoint<Integer>> createIntPointCreator(int uncollectedValue) {
        return new Point.UncollectedPointCreator<AgentStatPoint<Integer>>() {
            @Override
            public AgentStatPoint<Integer> createUnCollectedPoint(long xVal) {
                return new AgentStatPoint<>(xVal, uncollectedValue);
            }
        };
    }

    public static Point.UncollectedPointCreator<AgentStatPoint<Long>> createLongPointCreator(long uncollectedValue) {
        return new Point.UncollectedPointCreator<AgentStatPoint<Long>>() {
            @Override
            public AgentStatPoint<Long> createUnCollectedPoint(long xVal) {
                return new AgentStatPoint<>(xVal, uncollectedValue);
            }
        };
    }

    public static Point.UncollectedPointCreator<AgentStatPoint<Double>> createDoublePointCreator(double uncollectedValue) {
        return new Point.UncollectedPointCreator<AgentStatPoint<Double>>() {
            @Override
            public AgentStatPoint<Double> createUnCollectedPoint(long xVal) {
                return new AgentStatPoint<>(xVal, uncollectedValue);
            }
        };
    }

}
