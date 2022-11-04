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

package com.navercorp.pinpoint.web.vo.chart;

import com.navercorp.pinpoint.web.vo.stat.chart.agent.DoubleAgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.IntAgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.LongAgentStatPoint;

/**
 * @author Taejin Koo
 */
public class UncollectedPointCreatorFactory {

    public static Point.UncollectedPointCreator<IntAgentStatPoint> createIntPointCreator(int uncollectedValue) {
        return new Point.UncollectedPointCreator<IntAgentStatPoint>() {
            @Override
            public IntAgentStatPoint createUnCollectedPoint(long xVal) {
                return IntAgentStatPoint.ofUnCollected(xVal, uncollectedValue);
            }
        };
    }

    public static Point.UncollectedPointCreator<LongAgentStatPoint> createLongPointCreator(long uncollectedValue) {
        return new Point.UncollectedPointCreator<LongAgentStatPoint>() {
            @Override
            public LongAgentStatPoint createUnCollectedPoint(long xVal) {
                return LongAgentStatPoint.ofUnCollected(xVal, uncollectedValue);
            }
        };
    }

    public static Point.UncollectedPointCreator<DoubleAgentStatPoint> createDoublePointCreator(double uncollectedValue) {
        return new Point.UncollectedPointCreator<DoubleAgentStatPoint>() {
            @Override
            public DoubleAgentStatPoint createUnCollectedPoint(long xVal) {
                return DoubleAgentStatPoint.ofUnCollected(xVal, uncollectedValue);
            }
        };
    }

}
