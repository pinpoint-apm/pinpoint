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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class JoinMemorySampler implements ApplicationStatSampler<JoinMemoryBo> {

    @Override
    public AggreJoinMemoryBo sampleDataPoints(int index, long timestamp, List<JoinMemoryBo> joinMemoryBoList, JoinMemoryBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinMemoryBoList)) {
            return AggreJoinMemoryBo.createUncollectedObject(timestamp);
        }

        JoinMemoryBo joinMemoryBo = JoinMemoryBo.joinMemoryBoList(joinMemoryBoList, timestamp);
        String id = joinMemoryBo.getId();
        final JoinLongFieldBo heapUsedJoinValue = joinMemoryBo.getHeapUsedJoinValue();
        final JoinLongFieldBo nonHeapUsedJoinValue = joinMemoryBo.getNonHeapUsedJoinValue();

        AggreJoinMemoryBo aggreJoinMemoryBo = new AggreJoinMemoryBo(id, timestamp, heapUsedJoinValue, nonHeapUsedJoinValue);
        return aggreJoinMemoryBo;
    }
}
