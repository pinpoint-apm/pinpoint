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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class JoinActiveTraceSampler implements ApplicationStatSampler<JoinActiveTraceBo> {

    @Override
    public AggreJoinActiveTraceBo sampleDataPoints(int index, long timestamp, List<JoinActiveTraceBo> joinActiveTraceBoList, JoinActiveTraceBo previousDataPoint) {
        if (CollectionUtils.isEmpty(joinActiveTraceBoList)) {
            return AggreJoinActiveTraceBo.createUncollectedObject(timestamp);
        }

        JoinActiveTraceBo joinActiveTraceBo = JoinActiveTraceBo.joinActiveTraceBoList(joinActiveTraceBoList, timestamp);
        String id = joinActiveTraceBo.getId();
        int histogramSchemaType = joinActiveTraceBo.getHistogramSchemaType();
        short version = joinActiveTraceBo.getVersion();
        int totalCount = joinActiveTraceBo.getTotalCount();
        int maxTotalCount = joinActiveTraceBo.getMaxTotalCount();
        String maxTotalCountAgentId = joinActiveTraceBo.getMaxTotalCountAgentId();
        int minTotalCount = joinActiveTraceBo.getMinTotalCount();
        String minTotalCountAgentId = joinActiveTraceBo.getMinTotalCountAgentId();
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo = new AggreJoinActiveTraceBo(id, histogramSchemaType, version, totalCount, minTotalCount, minTotalCountAgentId, maxTotalCount, maxTotalCountAgentId, timestamp);

        return aggreJoinActiveTraceBo;
    }
}
