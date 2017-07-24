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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;

/**
 * @author minwoo.jung
 */
public class AggreJoinTransactionBo extends JoinTransactionBo implements AggregationStatData{

    public AggreJoinTransactionBo() {
    }

    public AggreJoinTransactionBo(String id, long collectInterval, long totalCount, long minTotalCount, String minTotalCountAgentId, long maxTotalCount, String maxTotalCountAgentId, long timestamp) {
        super(id, collectInterval, totalCount, minTotalCount, minTotalCountAgentId, maxTotalCount, maxTotalCountAgentId, timestamp);
    }

    public static AggreJoinTransactionBo createUncollectedObject(long timestamp) {
        AggreJoinTransactionBo aggreJoinTransactionBo = new AggreJoinTransactionBo();
        aggreJoinTransactionBo.setTimestamp(timestamp);
        return aggreJoinTransactionBo;
    }
}
