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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class JoinResponseTimeSampler implements ApplicationStatSampler<JoinResponseTimeBo> {

    @Override
    public AggreJoinResponseTimeBo sampleDataPoints(int index, long timestamp, List<JoinResponseTimeBo> joinResponseTimeBoList, JoinResponseTimeBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinResponseTimeBoList)) {
            return AggreJoinResponseTimeBo.createUncollectedObject(timestamp);
        }

        JoinResponseTimeBo joinResponseTimeBo = JoinResponseTimeBo.joinResponseTimeBoList(joinResponseTimeBoList, timestamp);
        String id = joinResponseTimeBo.getId();
        long avg = joinResponseTimeBo.getAvg();
        long minAvg = joinResponseTimeBo.getMinAvg();
        String minAvgAgentId = joinResponseTimeBo.getMinAvgAgentId();
        long maxAvg = joinResponseTimeBo.getMaxAvg();
        String maxAvgAgentId = joinResponseTimeBo.getMaxAvgAgentId();

        AggreJoinResponseTimeBo aggreJoinResponseTimeBo = new AggreJoinResponseTimeBo(id, timestamp, avg, minAvg, minAvgAgentId, maxAvg, maxAvgAgentId);
        return aggreJoinResponseTimeBo;
    }
}
