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
import com.navercorp.pinpoint.web.vo.stat.chart.agent.LongAgentStatPoint;

import java.util.Objects;

/**
 * @author Roy Kim
 */
public class SampledDirectBuffer implements SampledAgentStatDataPoint {

    public static final Long UNCOLLECTED_VALUE = -1L;
    public static final Point.UncollectedPointCreator<LongAgentStatPoint> UNCOLLECTED_POINT_CREATOR = UncollectedPointCreatorFactory.createLongPointCreator(UNCOLLECTED_VALUE);

    private final LongAgentStatPoint directCount;
    private final LongAgentStatPoint directMemoryUsed;
    private final LongAgentStatPoint mappedCount;
    private final LongAgentStatPoint mappedMemoryUsed;

    public SampledDirectBuffer(LongAgentStatPoint directCount, LongAgentStatPoint directMemoryUsed, LongAgentStatPoint mappedCount, LongAgentStatPoint mappedMemoryUsed) {
        this.directCount = Objects.requireNonNull(directCount, "directCount");
        this.directMemoryUsed = Objects.requireNonNull(directMemoryUsed, "directMemoryUsed");
        this.mappedCount = Objects.requireNonNull(mappedCount, "mappedCount");
        this.mappedMemoryUsed = Objects.requireNonNull(mappedMemoryUsed, "mappedMemoryUsed");
    }
    public LongAgentStatPoint getDirectCount() {
        return directCount;
    }

    public LongAgentStatPoint getDirectMemoryUsed() {
        return directMemoryUsed;
    }

    public LongAgentStatPoint getMappedCount() {
        return mappedCount;
    }

    public LongAgentStatPoint getMappedMemoryUsed() {
        return mappedMemoryUsed;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SampledDirectBuffer{");
        sb.append("directCount=").append(directCount);
        sb.append("directMemoryUsed=").append(directMemoryUsed);
        sb.append("mappedCount=").append(mappedCount);
        sb.append("mappedMemoryUsed=").append(mappedMemoryUsed);
        sb.append('}');
        return sb.toString();
    }
}
