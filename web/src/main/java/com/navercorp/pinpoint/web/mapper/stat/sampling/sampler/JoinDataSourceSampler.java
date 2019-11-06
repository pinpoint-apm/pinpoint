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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceListBo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
@Component
public class JoinDataSourceSampler implements ApplicationStatSampler<JoinDataSourceListBo> {

    @Override
    public AggreJoinDataSourceListBo sampleDataPoints(int index, long timestamp, List<JoinDataSourceListBo> joinDataSourceListBoList, JoinDataSourceListBo previousJoinDataSourceListBo) {
        if (CollectionUtils.isEmpty(joinDataSourceListBoList)) {
            return AggreJoinDataSourceListBo.createUncollectedObject(timestamp);
        }

        JoinDataSourceListBo joinDataSourceListBo = JoinDataSourceListBo.joinDataSourceListBoList(joinDataSourceListBoList, timestamp);
        String id = joinDataSourceListBo.getId();
        List<JoinDataSourceBo> joinDataSourceBoList = joinDataSourceListBo.getJoinDataSourceBoList();
        List<JoinDataSourceBo> aggreJoinDataSourceBoList = getJoinDataSourceBoList(timestamp, joinDataSourceBoList);

        AggreJoinDataSourceListBo aggreJoinDataSourceListBo = new AggreJoinDataSourceListBo(id, aggreJoinDataSourceBoList, timestamp);
        return aggreJoinDataSourceListBo;
    }

    public List<JoinDataSourceBo> getJoinDataSourceBoList(long timestamp, List<JoinDataSourceBo> joinDataSourceBoList) {
        List<JoinDataSourceBo> aggreJoinDataSourceBoList = new ArrayList<>(joinDataSourceBoList.size());
        for (JoinDataSourceBo ds : joinDataSourceBoList) {
            AggreJoinDataSourceBo dataSourceBo = new AggreJoinDataSourceBo(ds.getServiceTypeCode(), ds.getUrl(), ds.getAvgActiveConnectionSize(),
                    ds.getMinActiveConnectionSize(), ds.getMinActiveConnectionAgentId(), ds.getMaxActiveConnectionSize(), ds.getMaxActiveConnectionAgentId(), timestamp);
            aggreJoinDataSourceBoList.add(dataSourceBo);
        }
        return aggreJoinDataSourceBoList;
    }
}
