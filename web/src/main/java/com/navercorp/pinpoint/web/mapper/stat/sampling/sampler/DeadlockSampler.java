/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class DeadlockSampler implements AgentStatSampler<DeadlockThreadCountBo, SampledDeadlock> {

    private static final DownSampler<Integer> INTEGER_DOWN_SAMPLER = DownSamplers.getIntegerDownSampler(SampledDeadlock.UNCOLLECTED_COUNT);

    @Override
    public SampledDeadlock sampleDataPoints(int index, long timestamp, List<DeadlockThreadCountBo> deadlockThreadCountBoList, DeadlockThreadCountBo previousDataPoint) {
        List<Integer> deadlockedThreadCountList = filter(deadlockThreadCountBoList);

        AgentStatPoint<Integer> point = createPoint(timestamp, deadlockedThreadCountList);
        SampledDeadlock sampledDeadlock = new SampledDeadlock(point);

        return sampledDeadlock;
    }

    public List<Integer> filter(List<DeadlockThreadCountBo> deadlockThreadCountBoList) {
        List<Integer> deadlockedThreadCountList = new ArrayList<>(deadlockThreadCountBoList.size());

        for (DeadlockThreadCountBo deadlockThreadCountBo : deadlockThreadCountBoList) {
            deadlockedThreadCountList.add(deadlockThreadCountBo.getDeadlockedThreadCount());
        }
        return deadlockedThreadCountList;
    }

    private AgentStatPoint<Integer> createPoint(long timestamp, List<Integer> values) {
        if (values.isEmpty()) {
            return SampledDeadlock.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return new AgentStatPoint<>(
                timestamp,
                INTEGER_DOWN_SAMPLER.sampleMin(values),
                INTEGER_DOWN_SAMPLER.sampleMax(values),
                INTEGER_DOWN_SAMPLER.sampleAvg(values),
                INTEGER_DOWN_SAMPLER.sampleSum(values));
    }

}
