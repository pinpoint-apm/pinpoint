/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Roy Kim
 */
@Component
public class JoinDirectBufferSampler implements ApplicationStatSampler<JoinDirectBufferBo> {

    @Override
    public AggreJoinDirectBufferBo sampleDataPoints(int timeWindowIndex, long timestamp, List<JoinDirectBufferBo> joinDirectBufferBoList, JoinDirectBufferBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinDirectBufferBoList)) {
            return AggreJoinDirectBufferBo.createUncollectedObject(timestamp);
        }

        JoinDirectBufferBo joinDirectBufferBo = JoinDirectBufferBo.joinDirectBufferBoList(joinDirectBufferBoList, timestamp);

        String id = joinDirectBufferBo.getId();
        long avgDirectCount = joinDirectBufferBo.getAvgDirectCount();
        long minDirectCount = joinDirectBufferBo.getMinDirectCount();
        String minDirectCountAgentId = joinDirectBufferBo.getMinDirectCountAgentId();
        long maxDirectCount  = joinDirectBufferBo.getMaxDirectCount();
        String maxDirectCountAgentId = joinDirectBufferBo.getMaxDirectCountAgentId();

        long avgDirectMemoryUsed = joinDirectBufferBo.getAvgDirectMemoryUsed();
        long minDirectMemoryUsed = joinDirectBufferBo.getMinDirectMemoryUsed();
        String minDirectMemoryUsedAgentId = joinDirectBufferBo.getMinDirectMemoryUsedAgentId();
        long maxDirectMemoryUsed  = joinDirectBufferBo.getMaxDirectMemoryUsed();
        String maxDirectMemoryUsedAgentId = joinDirectBufferBo.getMaxDirectMemoryUsedAgentId();

        long avgMappedCount = joinDirectBufferBo.getAvgMappedCount();
        long minMappedCount = joinDirectBufferBo.getMinMappedCount();
        String minMappedCountAgentId = joinDirectBufferBo.getMinMappedCountAgentId();
        long maxMappedCount  = joinDirectBufferBo.getMaxMappedCount();
        String maxMappedCountAgentId = joinDirectBufferBo.getMaxMappedCountAgentId();

        long avgMappedMemoryUsed = joinDirectBufferBo.getAvgMappedMemoryUsed();
        long minMappedMemoryUsed = joinDirectBufferBo.getMinMappedMemoryUsed();
        String minMappedMemoryUsedAgentId = joinDirectBufferBo.getMinMappedMemoryUsedAgentId();
        long maxMappedMemoryUsed  = joinDirectBufferBo.getMaxMappedMemoryUsed();
        String maxMappedMemoryUsedAgentId = joinDirectBufferBo.getMaxMappedMemoryUsedAgentId();

        AggreJoinDirectBufferBo aggreJoinDirectBufferBo = new AggreJoinDirectBufferBo(id, avgDirectCount, maxDirectCount , maxDirectCountAgentId, minDirectCount, minDirectCountAgentId
                , avgDirectMemoryUsed, maxDirectMemoryUsed , maxDirectMemoryUsedAgentId, minDirectMemoryUsed, minDirectMemoryUsedAgentId
                , avgMappedCount, maxMappedCount , maxMappedCountAgentId, minMappedCount, minMappedCountAgentId
                , avgMappedMemoryUsed, maxMappedMemoryUsed , maxMappedMemoryUsedAgentId, minMappedMemoryUsed, minMappedMemoryUsedAgentId, timestamp);
        return aggreJoinDirectBufferBo;
    }
}
