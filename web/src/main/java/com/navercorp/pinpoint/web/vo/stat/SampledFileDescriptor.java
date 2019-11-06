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
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;

import java.util.Objects;

/**
 * @author Roy Kim
 */
public class SampledFileDescriptor implements SampledAgentStatDataPoint {

    public static final Long UNCOLLECTED_VALUE = -1L;
    public static final Point.UncollectedPointCreator<AgentStatPoint<Long>> UNCOLLECTED_POINT_CREATOR = new Point.UncollectedPointCreator<AgentStatPoint<Long>>() {
        @Override
        public AgentStatPoint<Long> createUnCollectedPoint(long xVal) {
            return new AgentStatPoint<>(xVal, UNCOLLECTED_VALUE);
        }
    };

    private final AgentStatPoint<Long> openFileDescriptorCount;

    public SampledFileDescriptor(AgentStatPoint<Long> openFileDescriptorCount) {
        this.openFileDescriptorCount = Objects.requireNonNull(openFileDescriptorCount, "openFileDescriptorCount");
    }

    public AgentStatPoint<Long> getOpenFileDescriptorCount() {
        return openFileDescriptorCount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledFileDescriptor{");
        sb.append("openFileDescriptorCount=").append(openFileDescriptorCount);
        sb.append('}');
        return sb.toString();
    }
}
