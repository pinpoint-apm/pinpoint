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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;

/**
 * @author minwoo.jung
 */
public class AggreJoinResponseTimeBo extends JoinResponseTimeBo implements AggregationStatData {

    public AggreJoinResponseTimeBo() {
    }

    public AggreJoinResponseTimeBo(String id, long timestamp, long avg, long minAvg, String minAvgAgentId, long maxAvg, String maxAvgAgentId) {
        super(id, timestamp, avg, minAvg, minAvgAgentId, maxAvg, maxAvgAgentId);
    }

    public static AggreJoinResponseTimeBo createUncollectedObject(long timestamp) {
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo = new AggreJoinResponseTimeBo();
        aggreJoinResponseTimeBo.setTimestamp(timestamp);
        return aggreJoinResponseTimeBo;
    }
}
