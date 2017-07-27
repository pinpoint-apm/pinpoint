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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTransactionBo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class JoinTransactionSampler implements ApplicationStatSampler<JoinTransactionBo> {

    @Override
    public AggreJoinTransactionBo sampleDataPoints(int index, long timestamp, List<JoinTransactionBo> joinTransactionBoList, JoinTransactionBo previousDataPoint) {
        if (joinTransactionBoList.size() == 0) {
            return AggreJoinTransactionBo.createUncollectedObject(timestamp);
        }

        JoinTransactionBo joinTransactionBo = JoinTransactionBo.joinTransactionBoLIst(joinTransactionBoList, timestamp);
        String id = joinTransactionBo.getId();
        long collectInterval = joinTransactionBo.getCollectInterval();
        long totalCount = joinTransactionBo.getTotalCount();
        long maxTotalCount = joinTransactionBo.getMaxTotalCount();
        String maxTotalCountAgentId = joinTransactionBo.getMaxTotalCountAgentId();
        long minTotalCount = joinTransactionBo.getMinTotalCount();
        String minTotalCountAgentId = joinTransactionBo.getMinTotalCountAgentId();
        AggreJoinTransactionBo aggreJoinTransactionBo = new AggreJoinTransactionBo(id, collectInterval, totalCount, minTotalCount, minTotalCountAgentId, maxTotalCount, maxTotalCountAgentId, timestamp);
        return aggreJoinTransactionBo;
    }
}
