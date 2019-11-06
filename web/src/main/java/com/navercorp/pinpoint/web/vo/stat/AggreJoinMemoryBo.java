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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;

/**
 * @author minwoo.jung
 */
public class AggreJoinMemoryBo extends JoinMemoryBo implements AggregationStatData {

    public AggreJoinMemoryBo() {
    }

    public AggreJoinMemoryBo(String id, long timestamp, long heapUsed, long minHeapUsed, long maxHeapUsed, String minHeapAgentId, String maxHeapAgentId, long nonHeapUsed, long minNonHeapUsed, long maxNonHeapUsed, String minNonHeapAgentId, String maxNonHeapAgentId) {
        super(id, timestamp, heapUsed, minHeapUsed, maxHeapUsed, minHeapAgentId, maxHeapAgentId, nonHeapUsed, minNonHeapUsed, maxNonHeapUsed, minNonHeapAgentId, maxNonHeapAgentId);
    }

    public static AggreJoinMemoryBo createUncollectedObject(long timestamp) {
        AggreJoinMemoryBo aggreJoinMemoryBo = new AggreJoinMemoryBo();
        aggreJoinMemoryBo.setTimestamp(timestamp);
        return aggreJoinMemoryBo;
    }
}
