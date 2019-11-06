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
package com.navercorp.pinpoint.web.vo.stat;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class AggreJoinDataSourceListBo extends JoinDataSourceListBo implements AggregationStatData {


    public AggreJoinDataSourceListBo() {
    }

    public AggreJoinDataSourceListBo(String id, List<JoinDataSourceBo> aggreJoinDataSourceBoList, long timestamp) {
        super(id, aggreJoinDataSourceBoList, timestamp);
    }

    public static AggreJoinDataSourceListBo createUncollectedObject(long timestamp) {
        AggreJoinDataSourceListBo aggreJoinDataSourceListBo = new AggreJoinDataSourceListBo();
        aggreJoinDataSourceListBo.setTimestamp(timestamp);
        return aggreJoinDataSourceListBo;
    }

    public List<AggreJoinDataSourceBo> getAggreJoinDataSourceBoList() {
        return castAggreJoinDataSourceBo(getJoinDataSourceBoList());
    }

    private List<AggreJoinDataSourceBo> castAggreJoinDataSourceBo(List<JoinDataSourceBo> joinDataSourceBoList) {
        List<AggreJoinDataSourceBo> aggreJoinDataSourceListBoList = new ArrayList<>(joinDataSourceBoList.size());

        for (JoinDataSourceBo joinDataSourceBo : joinDataSourceBoList) {
            aggreJoinDataSourceListBoList.add((AggreJoinDataSourceBo)joinDataSourceBo);
        }

        return aggreJoinDataSourceListBoList;
    }
}
