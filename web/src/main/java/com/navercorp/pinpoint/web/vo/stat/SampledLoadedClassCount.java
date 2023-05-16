/*
 * Copyright 2018 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPointCreatorFactory;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Objects;

public class SampledLoadedClassCount implements SampledAgentStatDataPoint {

    public static final Long UNCOLLECTED_VALUE = -1L;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Long>> UNCOLLECTED_POINT_CREATOR = UncollectedPointCreatorFactory.createLongPointCreator(UNCOLLECTED_VALUE);

    private final AgentStatPoint<Long> loadedClassCount;
    private final AgentStatPoint<Long> unloadedClassCount;

    public SampledLoadedClassCount(AgentStatPoint<Long> loadedClassCount, AgentStatPoint<Long> unloadedClassCount) {
        this.loadedClassCount = Objects.requireNonNull(loadedClassCount, "directCount");
        this.unloadedClassCount = Objects.requireNonNull(unloadedClassCount, "directMemoryUsed");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledLoadedClassCount{");
        sb.append("loadedClassCount=").append(loadedClassCount);
        sb.append("unloadedClassCount=").append(unloadedClassCount);
        sb.append('}');
        return sb.toString();
    }

    public AgentStatPoint<Long> getUnloadedClassCount() { return unloadedClassCount; }

    public AgentStatPoint<Long> getLoadedClassCount() { return loadedClassCount; }
}
