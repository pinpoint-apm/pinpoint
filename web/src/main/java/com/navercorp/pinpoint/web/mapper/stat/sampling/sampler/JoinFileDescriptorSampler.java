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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinFileDescriptorBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Roy Kim
 */
@Component
public class JoinFileDescriptorSampler implements ApplicationStatSampler<JoinFileDescriptorBo> {

    @Override
    public AggreJoinFileDescriptorBo sampleDataPoints(int timeWindowIndex, long timestamp, List<JoinFileDescriptorBo> joinFileDescriptorBoList, JoinFileDescriptorBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinFileDescriptorBoList)) {
            return AggreJoinFileDescriptorBo.createUncollectedObject(timestamp);
        }

        JoinFileDescriptorBo joinFileDescriptorBo = JoinFileDescriptorBo.joinFileDescriptorBoList(joinFileDescriptorBoList, timestamp);

        String id = joinFileDescriptorBo.getId();
        long openFileDescriptorCount = joinFileDescriptorBo.getAvgOpenFDCount();
        long minOpenFileDescriptor = joinFileDescriptorBo.getMinOpenFDCount();
        String minOpenFileDescriptorAgentId = joinFileDescriptorBo.getMinOpenFDCountAgentId();
        long maxOpenFileDescriptor  = joinFileDescriptorBo.getMaxOpenFDCount();
        String maxOpenFileDescriptorAgentId = joinFileDescriptorBo.getMaxOpenFDCountAgentId();

        AggreJoinFileDescriptorBo aggreJoinFileDescriptorBo = new AggreJoinFileDescriptorBo(id, openFileDescriptorCount, maxOpenFileDescriptor , maxOpenFileDescriptorAgentId, minOpenFileDescriptor, minOpenFileDescriptorAgentId, timestamp);
        return aggreJoinFileDescriptorBo;
    }
}
