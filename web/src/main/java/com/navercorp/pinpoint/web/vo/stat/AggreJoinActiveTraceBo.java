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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;

/**
 * @author minwoo.jung
 */
public class AggreJoinActiveTraceBo extends JoinActiveTraceBo implements AggregationStatData {

    public AggreJoinActiveTraceBo() {
    }

    public AggreJoinActiveTraceBo(String id, int histogramSchemaType, short version, int totalCount, int minTotalCount, String minTotalCountAgentId, int maxTotalCount, String maxTotalCountAgentId, long timestamp) {
        super(id, histogramSchemaType, version, totalCount, minTotalCount, minTotalCountAgentId, maxTotalCount, maxTotalCountAgentId, timestamp);
    }

    public AggreJoinActiveTraceBo(String id, int histogramSchemaType, short version, JoinIntFieldBo totalCountJoinValue, long timestamp) {
        super(id, histogramSchemaType, version, totalCountJoinValue, timestamp);
    }

    public static AggreJoinActiveTraceBo createUncollectedObject(long timestamp) {
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo = new AggreJoinActiveTraceBo();
        aggreJoinActiveTraceBo.setTimestamp(timestamp);
        return aggreJoinActiveTraceBo;
    }
}
