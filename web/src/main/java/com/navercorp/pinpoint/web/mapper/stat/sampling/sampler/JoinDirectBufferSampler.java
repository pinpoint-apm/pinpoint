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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
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
        final JoinLongFieldBo directCountJoinValue = joinDirectBufferBo.getDirectCountJoinValue();
        final JoinLongFieldBo directMemoryUsedJoinValue = joinDirectBufferBo.getDirectMemoryUsedJoinValue();
        final JoinLongFieldBo mappedCountJoinValue = joinDirectBufferBo.getMappedCountJoinValue();
        final JoinLongFieldBo mappedMemoryUsedJoinValue = joinDirectBufferBo.getMappedMemoryUsedJoinValue();

        AggreJoinDirectBufferBo aggreJoinDirectBufferBo = new AggreJoinDirectBufferBo(id, directCountJoinValue, directMemoryUsedJoinValue, mappedCountJoinValue, mappedMemoryUsedJoinValue, timestamp);
        return aggreJoinDirectBufferBo;
    }
}
