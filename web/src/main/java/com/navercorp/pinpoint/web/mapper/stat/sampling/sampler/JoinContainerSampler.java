/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinContainerBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinContainerBo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hyunjoon Cho
 */
@Component
public class JoinContainerSampler implements ApplicationStatSampler<JoinContainerBo> {

    @Override
    public AggreJoinContainerBo sampleDataPoints(int timeWindowIndex, long timestamp, List<JoinContainerBo> joinContainerBoList, JoinContainerBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinContainerBoList)) {
            return AggreJoinContainerBo.createUncollectedObject(timestamp);
        }

        JoinContainerBo joinContainerBo = JoinContainerBo.joinContainerBoList(joinContainerBoList, timestamp);

        String id = joinContainerBo.getId();
        final JoinDoubleFieldBo userCpuUsageJoinValue = joinContainerBo.getUserCpuUsageJoinValue();
        final JoinDoubleFieldBo systemCpuUsageJoinValue = joinContainerBo.getSystemCpuUsageJoinValue();
        final JoinLongFieldBo memoryMaxJoinValue = joinContainerBo.getMemoryMaxJoinValue();
        final JoinLongFieldBo memoryUsageJoinValue = joinContainerBo.getMemoryUsageJoinValue();

        AggreJoinContainerBo aggreJoinContainerBo = new AggreJoinContainerBo(id, userCpuUsageJoinValue, systemCpuUsageJoinValue, memoryMaxJoinValue, memoryUsageJoinValue, timestamp);
        return aggreJoinContainerBo;
    }

}