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

import com.navercorp.pinpoint.common.server.bo.stat.DeadlockBo;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.chart.UncollectedPoint;
import com.navercorp.pinpoint.web.vo.stat.SampledDeadlock;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.DownSamplers;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class DeadlockSampler implements AgentStatSampler<DeadlockBo, SampledDeadlock> {

    public static final DownSampler<Integer> INTEGER_DOWN_SAMPLER = DownSamplers.getIntegerDownSampler(DeadlockBo.UNCOLLECTED_INT_VALUE);

    @Override
    public SampledDeadlock sampleDataPoints(int index, long timestamp, List<DeadlockBo> deadlockBoList, DeadlockBo previousDataPoint) {
        List<Integer> deadlockedThreadCountList = new ArrayList<>(deadlockBoList.size());

        for (DeadlockBo deadlockBo : deadlockBoList) {
            deadlockedThreadCountList.add(deadlockBo.getDeadlockedThreadCount());
        }

        SampledDeadlock sampledDeadlock = new SampledDeadlock();
        sampledDeadlock.setDeadlockedThreadCount(createPoint(timestamp, deadlockedThreadCountList));

        return sampledDeadlock;
    }

    private Point<Long, Integer> createPoint(long timestamp, List<Integer> values) {
        if (values.isEmpty()) {
            return new UncollectedPoint<>(timestamp, DeadlockBo.UNCOLLECTED_INT_VALUE);
        } else {
            return new Point<>(
                    timestamp,
                    INTEGER_DOWN_SAMPLER.sampleMin(values),
                    INTEGER_DOWN_SAMPLER.sampleMax(values),
                    INTEGER_DOWN_SAMPLER.sampleAvg(values),
                    INTEGER_DOWN_SAMPLER.sampleSum(values));
        }
    }

}
