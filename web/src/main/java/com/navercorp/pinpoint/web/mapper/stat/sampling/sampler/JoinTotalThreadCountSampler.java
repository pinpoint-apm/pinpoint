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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JoinTotalThreadCountSampler implements ApplicationStatSampler<JoinTotalThreadCountBo> {
    @Override
    public AggreJoinTotalThreadCountBo sampleDataPoints(int index, long timestamp, List<JoinTotalThreadCountBo> dataPoints, JoinTotalThreadCountBo previousDataPoint) {
        if(CollectionUtils.isEmpty(dataPoints)) {
            return AggreJoinTotalThreadCountBo.createUncollectedObject(timestamp);
        }
        JoinTotalThreadCountBo joinTotalThreadCountBo = JoinTotalThreadCountBo.joinTotalThreadCountBoList(dataPoints, timestamp);

        String id = joinTotalThreadCountBo.getId();
        final JoinLongFieldBo totalThreadCountJoinValue = joinTotalThreadCountBo.getTotalThreadCountJoinValue();

        AggreJoinTotalThreadCountBo aggreJoinTotalThraedCountBo = new AggreJoinTotalThreadCountBo(id, timestamp, totalThreadCountJoinValue);
        return aggreJoinTotalThraedCountBo;
    }
}
